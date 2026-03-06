//
//  ContentView.swift
//  GraceNote
//
//  Created by 오승준 on 3/6/26.
//

import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(apiKey: geminiApiKey)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private var geminiApiKey: String {
        Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}
