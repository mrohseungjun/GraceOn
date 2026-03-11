import Foundation
import GoogleMobileAds
import UIKit

@MainActor
final class RewardedAdManager: NSObject, FullScreenContentDelegate {
    static let shared = RewardedAdManager()
    private static let iOSTestRewardedAdUnitId = "ca-app-pub-3940256099942544/1712485313"

    private weak var hostViewController: UIViewController?
    private var rewardedAd: RewardedAd?
    private var isLoading = false
    private var rewardEarned = false
    private var completion: ((String, String?) -> Void)?

    private override init() {
        super.init()
    }

    func configure(hostViewController: UIViewController) {
        self.hostViewController = hostViewController
        MobileAds.shared.start(completionHandler: nil)
        let appId = Bundle.main.object(forInfoDictionaryKey: "GADApplicationIdentifier") as? String ?? ""
        print("GraceOnAds iOS configured appId=\(appId) unitId=\(resolvedAdUnitId) configuredUnitId=\(configuredAdUnitId)")
        preload()
    }

    func show(completion: @escaping (String, String?) -> Void) {
        DispatchQueue.main.async {
            guard let hostViewController = self.hostViewController else {
                completion("failed", "광고를 표시할 화면을 찾지 못했습니다.")
                return
            }

            guard !self.resolvedAdUnitId.isEmpty else {
                completion("failed", "iOS 리워드 광고 설정이 비어 있습니다.")
                return
            }

            self.completion = completion

            if let rewardedAd = self.rewardedAd {
                self.present(rewardedAd, from: hostViewController)
                return
            }

            if self.isLoading {
                completion("failed", "광고를 준비 중입니다. 잠시 후 다시 시도해주세요.")
                return
            }

            self.loadRewardedAd { result in
                switch result {
                case .success(let rewardedAd):
                    self.present(rewardedAd, from: hostViewController)
                case .failure(let error):
                    let nsError = error as NSError
                    let message = "광고를 불러오지 못했습니다. (\(nsError.domain):\(nsError.code)) \(error.localizedDescription)"
                    print("GraceOnAds iOS load failed: domain=\(nsError.domain) code=\(nsError.code) desc=\(error.localizedDescription)")
                    self.completion?("failed", message)
                    self.completion = nil
                }
            }
        }
    }

    func preload() {
        DispatchQueue.main.async {
            guard !self.resolvedAdUnitId.isEmpty else { return }
            guard self.rewardedAd == nil, !self.isLoading else { return }
            self.loadRewardedAd(completion: nil)
        }
    }

    private var configuredAdUnitId: String {
        Bundle.main.object(forInfoDictionaryKey: "ADMOB_REWARDED_AD_UNIT_ID") as? String ?? ""
    }

    private var resolvedAdUnitId: String {
#if targetEnvironment(simulator)
        return Self.iOSTestRewardedAdUnitId
#else
        return configuredAdUnitId
#endif
    }

    private func loadRewardedAd(completion: ((Result<RewardedAd, Error>) -> Void)?) {
        isLoading = true
        let adUnitId = resolvedAdUnitId
        print("GraceOnAds iOS loading rewarded ad with unitId=\(adUnitId)")
        RewardedAd.load(with: adUnitId, request: Request()) { [weak self] ad, error in
            guard let self else { return }
            self.isLoading = false

            if let error {
                self.rewardedAd = nil
                let nsError = error as NSError
                print("GraceOnAds iOS RewardedAd.load error: domain=\(nsError.domain) code=\(nsError.code) desc=\(error.localizedDescription)")
                completion?(.failure(error))
                return
            }

            guard let ad else {
                completion?(.failure(NSError(domain: "GraceOnAd", code: -1)))
                return
            }

            self.rewardedAd = ad
            ad.fullScreenContentDelegate = self
            completion?(.success(ad))
        }
    }

    private func present(_ ad: RewardedAd, from hostViewController: UIViewController) {
        rewardEarned = false
        rewardedAd = nil
        ad.fullScreenContentDelegate = self
        ad.present(from: hostViewController) { [weak self] in
            self?.rewardEarned = true
        }
    }

    func adDidDismissFullScreenContent(_ ad: any FullScreenPresentingAd) {
        let status = rewardEarned ? "earned" : "dismissed"
        print("GraceOnAds iOS dismissed status=\(status)")
        completion?(status, nil)
        completion = nil
        preload()
    }

    func ad(_ ad: any FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: any Error) {
        print("GraceOnAds iOS present failed: \(error.localizedDescription)")
        completion?("failed", "광고를 표시하지 못했습니다. \(error.localizedDescription)")
        completion = nil
        preload()
    }
}
