//
//  ContentView.swift
//  GraceNote
//
//  Created by 오승준 on 3/6/26.
//

import SwiftUI
import ComposeApp
import Combine
import UserNotifications
import UIKit

@MainActor
final class InlineAdPlacementStore: ObservableObject {
    @Published var placement: String?
}

struct ComposeView: UIViewControllerRepresentable {
    @ObservedObject var inlineAdPlacementStore: InlineAdPlacementStore

    func makeCoordinator() -> Coordinator {
        Coordinator(inlineAdPlacementStore: inlineAdPlacementStore)
    }

    func makeUIViewController(context: Context) -> UIViewController {
        let coordinator = context.coordinator
        let viewController = MainViewControllerKt.MainViewController(
            apiBaseUrl: graceOnApiBaseUrl,
            supabaseAnonKey: supabaseAnonKey,
            appVersion: appVersion,
            onShareText: { text in
                coordinator.share(text: text)
            },
            onToggleDailyVerseNotification: { enabled in
                updateDailyVerseNotification(enabled: enabled.boolValue)
            },
            onOpenUrl: { rawUrl in
                guard let url = URL(string: rawUrl) else { return }
                DispatchQueue.main.async {
                    UIApplication.shared.open(url)
                }
            },
            onShowRewardedAd: { callback in
                coordinator.showRewardedAd(callback: callback)
            },
            onInlineAdPlacementChanged: { placement in
                coordinator.updateInlineAdPlacement(placement)
            }
        )

        coordinator.configure(hostViewController: viewController)
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private var graceOnApiBaseUrl: String {
        Bundle.main.object(forInfoDictionaryKey: "GRACEON_API_BASE_URL") as? String ?? ""
    }

    private var supabaseAnonKey: String {
        Bundle.main.object(forInfoDictionaryKey: "SUPABASE_ANON_KEY") as? String ?? ""
    }

    private var appVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
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

    final class Coordinator {
        weak var hostViewController: UIViewController?
        private let inlineAdPlacementStore: InlineAdPlacementStore

        init(inlineAdPlacementStore: InlineAdPlacementStore) {
            self.inlineAdPlacementStore = inlineAdPlacementStore
        }

        func configure(hostViewController: UIViewController) {
            self.hostViewController = hostViewController
            RewardedAdManager.shared.configure(hostViewController: hostViewController)
        }

        func share(text: String) {
            DispatchQueue.main.async { [weak self] in
                guard let hostViewController = self?.hostViewController else { return }

                let activityViewController = UIActivityViewController(
                    activityItems: [text],
                    applicationActivities: nil
                )

                if let popover = activityViewController.popoverPresentationController {
                    popover.sourceView = hostViewController.view
                    popover.sourceRect = CGRect(
                        x: hostViewController.view.bounds.midX,
                        y: hostViewController.view.bounds.maxY - 40,
                        width: 1,
                        height: 1
                    )
                }

                let presenter = hostViewController.presentedViewController ?? hostViewController
                presenter.present(activityViewController, animated: true)
            }
        }

        func showRewardedAd(callback: @escaping (String, String?) -> KotlinUnit) {
            RewardedAdManager.shared.show { status, message in
                _ = callback(status, message)
            }
        }

        func updateInlineAdPlacement(_ placement: String?) {
            DispatchQueue.main.async {
                self.inlineAdPlacementStore.placement = placement
            }
        }
    }
}

struct ContentView: View {
    @StateObject private var inlineAdPlacementStore = InlineAdPlacementStore()

    var body: some View {
        ZStack(alignment: .bottom) {
            ComposeView(inlineAdPlacementStore: inlineAdPlacementStore)
                .ignoresSafeArea()

            if let placement = inlineAdPlacementStore.placement {
                InlineBannerAdOverlay(placement: placement)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: inlineAdPlacementStore.placement)
            .onOpenURL { url in
                AuthBridgeKt.handleAuthCallbackUrl(url: url.absoluteString)
            }
    }
}

#Preview {
    ContentView()
}
