import SwiftUI
import Shared
import UniformTypeIdentifiers

struct SettingsScreen: View {
    @EnvironmentObject private var store: AppStore
    @State private var exportDocument: JSONDocument?
    @State private var showingExporter = false
    @State private var showingImporter = false
    @State private var importing = false
    @State private var message: String?

    var body: some View {
        Form {
            Section {
                Picker("theme", selection: Binding(
                    get: { store.data.settings.theme },
                    set: { store.updateSettings(theme: $0) }
                )) {
                    ForEach(ThemeMode.entries, id: \.self) { theme in
                        Text(theme.label).tag(theme)
                    }
                }
                Picker("first_day", selection: Binding(
                    get: { store.data.settings.firstDay },
                    set: { store.updateSettings(firstDay: $0) }
                )) {
                    ForEach(FirstDay.entries, id: \.self) { firstDay in
                        Text(firstDay.label).tag(firstDay)
                    }
                }
                Picker("date_format", selection: Binding(
                    get: { store.data.settings.dateFormat },
                    set: { store.updateSettings(dateFormat: $0) }
                )) {
                    ForEach(DateFormatStyle.entries, id: \.self) { style in
                        Text(style.label).tag(style)
                    }
                }
            }
            Section("data") {
                Button {
                    exportDocument = JSONDocument(text: store.repository.exportJson())
                    showingExporter = true
                } label: {
                    dataRow("export_data", description: "export_description", symbol: "square.and.arrow.up")
                }
                Button { showingImporter = true } label: {
                    dataRow("import_data", description: "import_description", symbol: "square.and.arrow.down")
                }
                if importing {
                    ProgressView().frame(maxWidth: .infinity)
                }
            }
        }
        .disabled(importing)
        .safeAreaInset(edge: .bottom) {
            Text(localized("app_version", Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0.0"))
                .font(.footnote)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
                .padding(16)
                .background(Color(uiColor: .systemGroupedBackground))
        }
        .navigationTitle("settings")
        .navigationBarTitleDisplayMode(.inline)
        .fileExporter(
            isPresented: $showingExporter, document: exportDocument, contentType: .json,
            defaultFilename: localized("export_file_name", String(describing: store.today))
        ) { result in
            switch result {
            case .success: message = localized("export_success")
            case .failure(let error):
                if !isCancellation(error) { message = localized("export_failed") }
            }
        }
        .fileImporter(isPresented: $showingImporter, allowedContentTypes: [.json, .text, .data], allowsMultipleSelection: false) { result in
            switch result {
            case .success(let urls):
                if let url = urls.first { importFile(url) }
            case .failure(let error):
                if !isCancellation(error) { message = localized("import_failed") }
            }
        }
        .alert("data", isPresented: Binding(get: { message != nil }, set: { if !$0 { message = nil } })) {
            Button("done", role: .cancel) { message = nil }
        } message: {
            Text(message ?? "")
        }
    }

    private func dataRow(_ title: LocalizedStringKey, description: LocalizedStringKey, symbol: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: symbol)
                .frame(width: 24)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                Text(description)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }

    private func importFile(_ url: URL) {
        importing = true
        Task { @MainActor in
            defer { importing = false }
            do {
                let text = try await Task.detached(priority: .userInitiated) {
                    try JSONDocument.read(from: url)
                }.value
                if let count = store.repository.importJson(text: text) {
                    message = localized("import_success", count.intValue)
                } else {
                    message = localized("import_invalid")
                }
            } catch {
                message = localized("import_failed")
            }
        }
    }

    private func isCancellation(_ error: Error) -> Bool {
        let error = error as NSError
        return error.domain == NSCocoaErrorDomain && error.code == NSUserCancelledError
    }
}
