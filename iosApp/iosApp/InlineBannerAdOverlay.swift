import Foundation
import GoogleMobileAds
import SwiftUI
import UIKit

private enum InlineBannerLoadState {
    case loading
    case loaded
    case failed
}

private enum InlineBannerPlacement: String {
    case homeFeed = "home_feed"
    case resultContent = "result_content"

    var configuredAdUnitId: String {
        switch self {
        case .homeFeed:
            return Bundle.main.object(forInfoDictionaryKey: "ADMOB_HOME_BANNER_AD_UNIT_ID") as? String ?? ""
        case .resultContent:
            return Bundle.main.object(forInfoDictionaryKey: "ADMOB_RESULT_BANNER_AD_UNIT_ID") as? String ?? ""
        }
    }

    var resolvedAdUnitId: String {
#if targetEnvironment(simulator)
        return "ca-app-pub-3940256099942544/2435281174"
#else
        return configuredAdUnitId
#endif
    }
}

struct InlineBannerAdOverlay: View {
    let placement: String

    var body: some View {
        GeometryReader { geometry in
            if let resolvedPlacement = InlineBannerPlacement(rawValue: placement) {
                VStack {
                    Spacer()
                    InlineBannerAdCard(
                        placement: resolvedPlacement,
                        availableWidth: max(geometry.size.width - 32, 320)
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, geometry.safeAreaInsets.bottom + 96)
                }
            }
        }
    }
}

private struct InlineBannerAdCard: View {
    let placement: InlineBannerPlacement
    let availableWidth: CGFloat
    @State private var loadState: InlineBannerLoadState = .loading

    var body: some View {
        if loadState != .failed {
        VStack(alignment: .leading, spacing: 8) {
            Text("후원 광고")
                .font(.caption.weight(.semibold))
                .foregroundStyle(Color.white.opacity(0.72))

            ZStack {
                InlineBannerAdView(
                    placement: placement,
                    availableWidth: availableWidth,
                    loadState: $loadState
                )
                .opacity(loadState == .loaded ? 1 : 0)

                if loadState != .loaded {
                    VStack(alignment: .leading, spacing: 8) {
                        ProgressView()
                            .progressViewStyle(.linear)
                            .tint(Color.white.opacity(0.8))
                        Text("광고 불러오는 중...")
                            .font(.caption)
                            .foregroundStyle(Color.white.opacity(0.72))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            .frame(height: 60)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .fill(Color.black.opacity(0.22))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(Color.white.opacity(0.14), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.18), radius: 10, x: 0, y: 6)
        }
    }
}

private struct InlineBannerAdView: UIViewRepresentable {
    let placement: InlineBannerPlacement
    let availableWidth: CGFloat
    @Binding var loadState: InlineBannerLoadState

    func makeCoordinator() -> Coordinator {
        Coordinator(loadState: $loadState)
    }

    func makeUIView(context: Context) -> BannerView {
        let bannerView = BannerView(adSize: adSize)
        bannerView.adUnitID = placement.resolvedAdUnitId
        bannerView.rootViewController = BannerRootViewControllerProvider.topViewController()
        bannerView.delegate = context.coordinator
        context.coordinator.loadedPlacement = placement.rawValue
        context.coordinator.loadedWidth = availableWidth
        loadState = .loading
        bannerView.load(Request())
        return bannerView
    }

    func updateUIView(_ uiView: BannerView, context: Context) {
        let shouldReload =
            context.coordinator.loadedPlacement != placement.rawValue ||
                abs(context.coordinator.loadedWidth - availableWidth) > 1

        guard shouldReload else { return }

        uiView.adSize = adSize
        uiView.adUnitID = placement.resolvedAdUnitId
        uiView.rootViewController = BannerRootViewControllerProvider.topViewController()
        context.coordinator.loadedPlacement = placement.rawValue
        context.coordinator.loadedWidth = availableWidth
        loadState = .loading
        uiView.load(Request())
    }

    private var adSize: AdSize {
        currentOrientationAnchoredAdaptiveBanner(width: availableWidth)
    }

    final class Coordinator: NSObject, BannerViewDelegate {
        private var loadState: Binding<InlineBannerLoadState>
        var loadedPlacement: String?
        var loadedWidth: CGFloat = 0

        init(loadState: Binding<InlineBannerLoadState>) {
            self.loadState = loadState
        }

        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
            DispatchQueue.main.async {
                self.loadState.wrappedValue = .loaded
            }
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
            let nsError = error as NSError
            DispatchQueue.main.async {
                self.loadState.wrappedValue = .failed
            }
            print(
                "GraceOnAds iOS banner load failed: domain=\(nsError.domain) code=\(nsError.code) desc=\(error.localizedDescription)"
            )
        }
    }
}

private enum BannerRootViewControllerProvider {
    static func topViewController(
        base: UIViewController? = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController
    ) -> UIViewController? {
        if let navigationController = base as? UINavigationController {
            return topViewController(base: navigationController.visibleViewController)
        }

        if let tabBarController = base as? UITabBarController,
           let selectedViewController = tabBarController.selectedViewController {
            return topViewController(base: selectedViewController)
        }

        if let presentedViewController = base?.presentedViewController {
            return topViewController(base: presentedViewController)
        }

        return base
    }
}
