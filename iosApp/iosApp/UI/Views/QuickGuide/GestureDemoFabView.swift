import Shared
import SwiftUI

enum DemoGesture {
    case click
    case longClick

    fileprivate var releaseTime: Double {
        switch self {
        case .click: 0.65
        case .longClick: 1.4
        }
    }

    fileprivate var holdScale: CGFloat {
        self == .longClick ? 1.7 : 1
    }

    fileprivate var holdOpacity: Double {
        self == .longClick ? 0.6 : 1
    }

    fileprivate var cycleDuration: Double {
        switch self {
        case .click: 2.2
        case .longClick: 2.8
        }
    }
}

struct GestureDemoFabView: View {
    let systemImage: String
    let gesture: DemoGesture

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private let primary = Color(SharedRes.colors().primary.getUIColor())
    private let fabSize: CGFloat = 49
    private let demoSize: CGFloat = 62
    private let ringWidth: CGFloat = 3

    var body: some View {
        Group {
            if reduceMotion {
                content(GestureDemoFrame())
            } else {
                content(GestureDemoFrame())
                    .keyframeAnimator(initialValue: GestureDemoFrame(), repeating: true) { _, frame in
                        content(frame)
                    } keyframes: { _ in
                        keyframes
                    }
            }
        }
        .frame(width: demoSize, height: demoSize)
        .accessibilityHidden(true)
    }

    private func content(_ frame: GestureDemoFrame) -> some View {
        ZStack {
            Circle()
                .stroke(primary, lineWidth: ringWidth)
                .frame(width: fabSize, height: fabSize)
                .scaleEffect(1 + (demoSize / fabSize - 1) * frame.rippleProgress)
                .opacity(frame.rippleProgress > 0 && frame.rippleProgress < 1 ? 0.6 * (1 - frame.rippleProgress) : 0)
            Image(systemName: systemImage)
                .font(.system(size: 19, weight: .bold))
                .foregroundStyle(primary)
                .frame(width: fabSize, height: fabSize)
                .background(Color(.secondarySystemBackground), in: Circle())
                .overlay { Circle().strokeBorder(Color(.separator), lineWidth: 0.5) }
                .shadow(color: .black.opacity(0.12), radius: 3, y: 1)
                .scaleEffect(frame.fabScale)
            Circle()
                .fill(Color.gray.opacity(0.5))
                .frame(width: 26, height: 26)
                .scaleEffect(frame.touchScale)
                .opacity(frame.touchOpacity)
        }
    }

    @KeyframesBuilder<GestureDemoFrame>
    private var keyframes: some Keyframes<GestureDemoFrame> {
        let pressStart = 0.4
        let pressIn = 0.12
        let release = gesture.releaseTime
        let releaseOut = 0.18
        let rippleDuration = 0.6
        let cycle = gesture.cycleDuration
        KeyframeTrack(\.fabScale) {
            CubicKeyframe(1, duration: pressStart)
            CubicKeyframe(0.9, duration: pressIn)
            LinearKeyframe(0.9, duration: release - pressStart - pressIn)
            CubicKeyframe(1, duration: releaseOut)
            LinearKeyframe(1, duration: cycle - release - releaseOut)
        }
        KeyframeTrack(\.touchOpacity) {
            LinearKeyframe(0, duration: pressStart)
            LinearKeyframe(1, duration: pressIn)
            LinearKeyframe(gesture.holdOpacity, duration: release - pressStart - pressIn)
            LinearKeyframe(0, duration: releaseOut)
            LinearKeyframe(0, duration: cycle - release - releaseOut)
        }
        KeyframeTrack(\.touchScale) {
            LinearKeyframe(1, duration: pressStart + pressIn)
            CubicKeyframe(gesture.holdScale, duration: release - pressStart - pressIn)
            LinearKeyframe(gesture.holdScale, duration: releaseOut)
            MoveKeyframe(1)
            LinearKeyframe(1, duration: cycle - release - releaseOut)
        }
        KeyframeTrack(\.rippleProgress) {
            LinearKeyframe(0, duration: release)
            CubicKeyframe(1, duration: rippleDuration)
            MoveKeyframe(0)
            LinearKeyframe(0, duration: cycle - release - rippleDuration)
        }
    }
}

private struct GestureDemoFrame {
    var fabScale: CGFloat = 1
    var touchOpacity: Double = 0
    var touchScale: CGFloat = 1
    var rippleProgress: CGFloat = 0
}

#Preview {
    HStack(spacing: 24) {
        GestureDemoFabView(systemImage: "location.fill", gesture: .click)
        GestureDemoFabView(systemImage: "location.north.line.fill", gesture: .longClick)
    }
    .padding()
}
