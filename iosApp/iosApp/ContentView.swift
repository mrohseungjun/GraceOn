//
//  ContentView.swift
//  GraceNote
//
//  Created by 오승준 on 3/6/26.
//

import SwiftUI
import ComposeApp
import UserNotifications
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            apiKey: geminiApiKey,
            appVersion: appVersion,
            onShareText: { text in
                share(text: text)
            },
            onToggleDailyVerseNotification: { enabled in
                updateDailyVerseNotification(enabled: enabled.boolValue)
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private var geminiApiKey: String {
        Bundle.main.object(forInfoDictionaryKey: "GEMINI_API_KEY") as? String ?? ""
    }

    private var appVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
    }

    private func share(text: String) {
        let activityViewController = UIActivityViewController(
            activityItems: [text],
            applicationActivities: nil
        )

        DispatchQueue.main.async {
            topViewController()?.present(activityViewController, animated: true)
        }
    }

    private func updateDailyVerseNotification(enabled: Bool) {
        let center = UNUserNotificationCenter.current()

        guard enabled else {
            center.removePendingNotificationRequests(withIdentifiers: ["daily-verse-reminder"])
            return
        }

        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }

            let content = UNMutableNotificationContent()
            content.title = "오늘의 말씀"
            content.body = "GraceOn과 함께 오늘 내게 주시는 말씀을 만나보세요."
            content.sound = .default

            var dateComponents = DateComponents()
            dateComponents.hour = 9
            dateComponents.minute = 0

            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
            let request = UNNotificationRequest(
                identifier: "daily-verse-reminder",
                content: content,
                trigger: trigger
            )

            center.removePendingNotificationRequests(withIdentifiers: ["daily-verse-reminder"])
            center.add(request)
        }
    }

    private func topViewController(
        base: UIViewController? = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first(where: { $0.isKeyWindow })?
            .rootViewController
    ) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }
        if let tab = base as? UITabBarController {
            return topViewController(base: tab.selectedViewController)
        }
        if let presented = base?.presentedViewController {
            return topViewController(base: presented)
        }
        return base
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
