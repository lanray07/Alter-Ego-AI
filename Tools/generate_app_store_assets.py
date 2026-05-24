from __future__ import annotations

import json
import math
from pathlib import Path
from typing import Iterable, Sequence

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parents[1]
ASSET_ROOT = ROOT / "AppStoreAssets"
SCREENSHOT_ROOT = ASSET_ROOT / "Screenshots"
ICON_EXPORT_ROOT = ASSET_ROOT / "AppIcon"
SUBSCRIPTION_REVIEW_ROOT = ASSET_ROOT / "SubscriptionReview"
SUBSCRIPTION_IMAGE_ROOT = ASSET_ROOT / "SubscriptionImages"
XCASSETS_ROOT = ROOT / "AlterEgoAI" / "Resources" / "Assets.xcassets"
APPICONSET_ROOT = XCASSETS_ROOT / "AppIcon.appiconset"
ACCENT_ROOT = XCASSETS_ROOT / "AccentColor.colorset"

FONT_REGULAR = Path("C:/Windows/Fonts/segoeui.ttf")
FONT_BOLD = Path("C:/Windows/Fonts/segoeuib.ttf")
FONT_LIGHT = Path("C:/Windows/Fonts/segoeuil.ttf")

BG = (4, 5, 16)
PURPLE = (92, 37, 255)
BLUE = (28, 112, 255)
CYAN = (38, 238, 255)
GREEN = (76, 255, 176)
WHITE = (248, 250, 255)
MUTED = (174, 184, 211)
CARD = (17, 18, 38)


def font(size: int, weight: str = "regular") -> ImageFont.FreeTypeFont:
    path = {"regular": FONT_REGULAR, "bold": FONT_BOLD, "light": FONT_LIGHT}.get(weight, FONT_REGULAR)
    return ImageFont.truetype(str(path), size)


def lerp(a: int, b: int, t: float) -> int:
    return int(a + (b - a) * t)


def gradient(size: tuple[int, int], stops: Sequence[tuple[float, tuple[int, int, int]]]) -> Image.Image:
    width, height = size
    image = Image.new("RGB", size, BG)
    pixels = image.load()
    for y in range(height):
        v = y / max(1, height - 1)
        left = stops[0]
        right = stops[-1]
        for idx in range(len(stops) - 1):
            if stops[idx][0] <= v <= stops[idx + 1][0]:
                left, right = stops[idx], stops[idx + 1]
                break
        span = max(0.001, right[0] - left[0])
        t = (v - left[0]) / span
        color = tuple(lerp(left[1][i], right[1][i], t) for i in range(3))
        for x in range(width):
            side = x / max(1, width - 1)
            glow = 0.18 * math.sin(side * math.pi)
            pixels[x, y] = tuple(min(255, int(c + glow * [20, 90, 120][i])) for i, c in enumerate(color))
    return image


def add_radial_glow(base: Image.Image, center: tuple[int, int], radius: int, color: tuple[int, int, int], alpha: int) -> None:
    overlay = Image.new("RGBA", base.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    for i in range(radius, 0, -10):
        opacity = int(alpha * (1 - i / radius) ** 1.8)
        draw.ellipse((center[0] - i, center[1] - i, center[0] + i, center[1] + i), fill=(*color, opacity))
    base.alpha_composite(overlay)


def rounded_rect(
    image: Image.Image,
    xy: tuple[int, int, int, int],
    radius: int,
    fill: tuple[int, int, int, int],
    outline: tuple[int, int, int, int] | None = None,
    width: int = 1,
    glow: tuple[int, int, int, int] | None = None,
) -> None:
    if glow:
        blur = Image.new("RGBA", image.size, (0, 0, 0, 0))
        blur_draw = ImageDraw.Draw(blur)
        blur_draw.rounded_rectangle(xy, radius=radius, fill=glow)
        blur = blur.filter(ImageFilter.GaussianBlur(radius=max(8, radius // 2)))
        image.alpha_composite(blur)
    shape = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(shape)
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)
    image.alpha_composite(shape)


def text(draw: ImageDraw.ImageDraw, xy: tuple[int, int], value: str, size: int, color=WHITE, weight="regular", anchor=None) -> None:
    draw.text(xy, value, font=font(size, weight), fill=color, anchor=anchor)


def wrapped_text(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int],
    value: str,
    size: int,
    max_width: int,
    fill=WHITE,
    weight="regular",
    line_spacing: int = 10,
) -> int:
    words = value.split()
    lines: list[str] = []
    current = ""
    fnt = font(size, weight)
    for word in words:
        proposed = word if not current else f"{current} {word}"
        if draw.textlength(proposed, font=fnt) <= max_width:
            current = proposed
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)

    x, y = xy
    for line in lines:
        draw.text((x, y), line, font=fnt, fill=fill)
        y += size + line_spacing
    return y


def draw_orbit_icon(image: Image.Image, center: tuple[int, int], radius: int, color=CYAN, width: int = 5) -> None:
    cx, cy = center
    for angle in (0, 55, -55):
        box = (cx - radius, cy - radius // 2, cx + radius, cy + radius // 2)
        layer = Image.new("RGBA", image.size, (0, 0, 0, 0))
        ld = ImageDraw.Draw(layer)
        ld.ellipse(box, outline=(*color, 180), width=width)
        layer = layer.rotate(angle, center=center)
        image.alpha_composite(layer)
    draw = ImageDraw.Draw(image)
    draw.ellipse((cx - radius // 4, cy - radius // 4, cx + radius // 4, cy + radius // 4), fill=(*WHITE, 235))


def draw_phone_frame(base: Image.Image, xy: tuple[int, int], size: tuple[int, int], screen_title: str, screen_kind: str) -> None:
    x, y = xy
    w, h = size
    rounded_rect(base, (x, y, x + w, y + h), w // 10, (6, 7, 18, 255), (75, 230, 255, 90), 3, (30, 215, 255, 55))
    rounded_rect(base, (x + 20, y + 20, x + w - 20, y + h - 20), w // 12, (9, 11, 28, 255))
    d = ImageDraw.Draw(base)
    rounded_rect(base, (x + w // 2 - 85, y + 36, x + w // 2 + 85, y + 54), 10, (30, 33, 54, 255))

    sx, sy, sw = x + 58, y + 88, w - 116
    text(d, (sx, sy), "ALTER EGO AI", 22, CYAN, "bold")
    text(d, (sx, sy + 42), screen_title, 44, WHITE, "bold")

    if screen_kind == "dashboard":
        draw_dashboard(base, sx, sy + 115, sw)
    elif screen_kind == "chat":
        draw_chat(base, sx, sy + 115, sw)
    elif screen_kind == "missions":
        draw_missions(base, sx, sy + 115, sw)
    elif screen_kind == "insights":
        draw_insights(base, sx, sy + 115, sw)
    elif screen_kind == "share":
        draw_share(base, sx, sy + 115, sw)
    else:
        draw_onboarding(base, sx, sy + 115, sw)


def draw_onboarding(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    rounded_rect(base, (x, y, x + w, y + 360), 34, (18, 19, 45, 245), (45, 238, 255, 95), 2)
    draw_orbit_icon(base, (x + w - 95, y + 95), 58, CYAN, 4)
    text(d, (x + 28, y + 28), "Your Future Self", 24, MUTED, "bold")
    wrapped_text(d, (x + 28, y + 72), "I become disciplined, calm, focused, and impossible to ignore.", 32, w - 190, WHITE, "bold", 8)
    for i, label in enumerate(["Discipline", "Confidence", "Focus"]):
        pill_x = x + 28 + i * 165
        rounded_rect(base, (pill_x, y + 245, pill_x + 148, y + 296), 25, (38, 238, 255, 38), (38, 238, 255, 110))
        text(d, (pill_x + 22, y + 258), label, 20, CYAN, "bold")
    for idx, line in enumerate(["Day 1: 20-minute workout", "Day 2: no scrolling challenge", "Day 3: focus sprint + journal"]):
        yy = y + 405 + idx * 84
        rounded_rect(base, (x, yy, x + w, yy + 62), 24, (255, 255, 255, 20))
        text(d, (x + 24, yy + 17), line, 24, WHITE, "regular")


def draw_dashboard(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    rounded_rect(base, (x, y, x + w, y + 310), 34, (20, 18, 48, 250), (38, 238, 255, 100), 2, (92, 37, 255, 50))
    text(d, (x + 28, y + 28), "Olan Prime", 40, WHITE, "bold")
    text(d, (x + 28, y + 83), "Level 10 Focused", 25, CYAN, "bold")
    d.arc((x + w - 185, y + 52, x + w - 45, y + 192), -90, 250, fill=CYAN, width=14)
    d.arc((x + w - 185, y + 52, x + w - 45, y + 192), 250, 270, fill=(255, 255, 255, 55), width=14)
    text(d, (x + w - 115, y + 96), "74%", 25, WHITE, "bold", anchor="mm")
    for i, (label, value) in enumerate([("Life XP", "2,480"), ("Streak", "7"), ("Score", "91")]):
        bx = x + 28 + i * ((w - 78) // 3)
        rounded_rect(base, (bx, y + 205, bx + 145, y + 268), 20, (255, 255, 255, 20))
        text(d, (bx + 18, y + 218), value, 25, CYAN, "bold")
        text(d, (bx + 18, y + 248), label, 15, MUTED, "regular")
    for idx, (label, xp) in enumerate([("30-minute focus session", "+55 XP"), ("Read 10 pages", "+35 XP"), ("Journal reflection", "+35 XP")]):
        yy = y + 355 + idx * 88
        rounded_rect(base, (x, yy, x + w, yy + 68), 24, (255, 255, 255, 20))
        d.ellipse((x + 23, yy + 21, x + 49, yy + 47), outline=GREEN if idx == 0 else CYAN, width=4)
        if idx == 0:
            d.line((x + 29, yy + 34, x + 36, yy + 42, x + 47, yy + 27), fill=GREEN, width=4)
        text(d, (x + 68, yy + 18), label, 24, WHITE, "regular")
        text(d, (x + w - 88, yy + 19), xp, 22, CYAN, "bold")


def draw_missions(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    for idx, (category, title, xp) in enumerate([
        ("BODY", "20-minute workout", "+60 XP"),
        ("FOCUS", "Deep work sprint", "+55 XP"),
        ("MIND", "Journal the lesson", "+35 XP"),
        ("SOCIAL", "Send one confident message", "+50 XP"),
    ]):
        yy = y + idx * 128
        rounded_rect(base, (x, yy, x + w, yy + 98), 28, (18, 19, 45, 245), (255, 255, 255, 28), 1)
        rounded_rect(base, (x + 24, yy + 24, x + 88, yy + 74), 20, (38, 238, 255, 35), (38, 238, 255, 100))
        text(d, (x + 108, yy + 20), title, 27, WHITE, "bold")
        text(d, (x + 108, yy + 58), category, 17, MUTED, "bold")
        text(d, (x + w - 110, yy + 37), xp, 22, CYAN, "bold")
    rounded_rect(base, (x, y + 545, x + w, y + 720), 34, (92, 37, 255, 105), (38, 238, 255, 85), 2)
    text(d, (x + 28, y + 582), "Comeback Plan", 31, WHITE, "bold")
    wrapped_text(d, (x + 28, y + 626), "Missed days are data, not identity. Win the next hour.", 25, w - 56, MUTED, "regular")


def draw_chat(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    bubbles = [
        ("assistant", "Your future self is watching. Start with one mission."),
        ("user", "I feel behind today."),
        ("assistant", "You are not behind. You are at the decision point. Win the next 20 minutes."),
        ("assistant", "Motivational coaching only. Not therapy or medical advice."),
    ]
    yy = y
    for role, line in bubbles:
        bubble_w = int(w * (0.78 if role == "assistant" else 0.66))
        bx = x if role == "assistant" else x + w - bubble_w
        fill = (18, 19, 45, 245) if role == "assistant" else (28, 112, 255, 210)
        rounded_rect(base, (bx, yy, bx + bubble_w, yy + 118), 30, fill, (38, 238, 255, 70) if role == "assistant" else None)
        wrapped_text(d, (bx + 25, yy + 24), line, 24, bubble_w - 50, WHITE if role != "assistant" else (232, 237, 255), "regular", 6)
        yy += 150
    rounded_rect(base, (x, y + 640, x + w, y + 710), 28, (255, 255, 255, 20))
    text(d, (x + 25, y + 662), "Ask for the next move...", 24, MUTED, "regular")


def draw_insights(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    for i, (label, value) in enumerate([("Consistency", "86%"), ("Strongest", "Focus"), ("Streak", "14")]):
        bx = x + i * ((w - 28) // 3)
        rounded_rect(base, (bx, y, bx + 150, y + 122), 28, (18, 19, 45, 245), (255, 255, 255, 25))
        text(d, (bx + 20, y + 24), value, 31, CYAN, "bold")
        text(d, (bx + 20, y + 71), label, 16, MUTED, "regular")
    chart_y = y + 170
    rounded_rect(base, (x, chart_y, x + w, chart_y + 320), 34, (18, 19, 45, 245), (38, 238, 255, 70))
    points = [(x + 34 + i * 72, chart_y + 250 - v) for i, v in enumerate([40, 80, 60, 140, 110, 190, 230])]
    d.line(points, fill=CYAN, width=7, joint="curve")
    for px, py in points:
        d.ellipse((px - 9, py - 9, px + 9, py + 9), fill=CYAN)
    text(d, (x + 28, chart_y + 24), "XP Trend", 27, WHITE, "bold")
    for idx, (title, summary) in enumerate([("Day 1 Baseline", "Started with discipline."), ("Level 10 Unlocked", "Focused identity activated.")]):
        yy = chart_y + 370 + idx * 100
        rounded_rect(base, (x, yy, x + w, yy + 76), 24, (255, 255, 255, 18))
        text(d, (x + 24, yy + 15), title, 24, WHITE, "bold")
        text(d, (x + 24, yy + 48), summary, 17, MUTED, "regular")


def draw_share(base: Image.Image, x: int, y: int, w: int) -> None:
    d = ImageDraw.Draw(base)
    rounded_rect(base, (x, y, x + w, y + 480), 42, (10, 14, 38, 255), (38, 238, 255, 95), 2, (38, 238, 255, 45))
    text(d, (x + 35, y + 35), "DAY 7", 26, CYAN, "bold")
    wrapped_text(d, (x + 35, y + 98), "Becoming my alter ego.", 50, w - 70, WHITE, "bold", 8)
    text(d, (x + 35, y + 248), "Level 10 Focused", 30, WHITE, "bold")
    text(d, (x + 35, y + 292), "I completed 30 missions this week.", 24, MUTED, "regular")
    draw_orbit_icon(base, (x + w - 115, y + 350), 72, CYAN, 6)
    rounded_rect(base, (x, y + 545, x + w, y + 650), 30, (92, 37, 255, 115), (255, 255, 255, 35))
    text(d, (x + 28, y + 578), "Native share sheet. Private until you share.", 24, WHITE, "bold")


def draw_check(draw: ImageDraw.ImageDraw, x: int, y: int, color: tuple[int, int, int] = GREEN) -> None:
    draw.line((x, y + 12, x + 9, y + 22, x + 27, y), fill=color, width=5, joint="curve")


def draw_paywall_card(
    base: Image.Image,
    xy: tuple[int, int],
    size: tuple[int, int],
    title: str,
    price: str,
    subtitle: str,
    features: Sequence[str],
    selected: bool,
) -> None:
    x, y = xy
    w, h = size
    fill = (17, 21, 50, 248) if selected else (255, 255, 255, 18)
    outline = (38, 238, 255, 170) if selected else (255, 255, 255, 42)
    glow = (38, 238, 255, 70) if selected else None
    rounded_rect(base, (x, y, x + w, y + h), 30, fill, outline, 2, glow)
    d = ImageDraw.Draw(base)
    text(d, (x + 28, y + 24), title, 30, WHITE, "bold")
    text(d, (x + 28, y + 68), price, 24, CYAN if selected else MUTED, "bold")
    text(d, (x + 28, y + 104), subtitle, 18, MUTED, "regular")
    badge_fill = (38, 238, 255, 52) if selected else (255, 255, 255, 18)
    badge_outline = (38, 238, 255, 130) if selected else (255, 255, 255, 45)
    rounded_rect(base, (x + w - 122, y + 27, x + w - 28, y + 67), 20, badge_fill, badge_outline)
    text(d, (x + w - 75, y + 37), "SELECTED" if selected else "PLAN", 13, CYAN if selected else MUTED, "bold", anchor="ma")
    feature_y = y + 156
    for feature in features[:4]:
        draw_check(d, x + 30, feature_y + 3, GREEN if selected else CYAN)
        wrapped_text(d, (x + 70, feature_y), feature, 19, w - 108, WHITE, "regular", 5)
        feature_y += 47


def draw_subscription_review_screenshot(
    path: Path,
    plan_title: str,
    price: str,
    subtitle: str,
    features: Sequence[str],
    product_id: str,
    product_label: str,
) -> None:
    size = (1242, 2688)
    w, h = size
    base = gradient(size, [(0, (3, 4, 16)), (0.44, (17, 16, 56)), (1, (2, 20, 35))]).convert("RGBA")
    add_radial_glow(base, (int(w * 0.82), int(h * 0.16)), int(w * 0.55), CYAN, 120)
    add_radial_glow(base, (int(w * 0.13), int(h * 0.78)), int(w * 0.58), PURPLE, 135)
    d = ImageDraw.Draw(base)

    text(d, (92, 120), "ALTER EGO AI", 44, CYAN, "bold")
    wrapped_text(d, (92, 188), "Unlock your future self.", 86, 960, WHITE, "bold", 10)
    wrapped_text(d, (92, 405), "Premium identity coaching, custom missions, advanced insights, and cinematic progress tools for general wellness and habit-building.", 35, 980, (219, 228, 250), "regular", 12)

    rounded_rect(base, (92, 620, 1150, 1210), 44, (11, 14, 36, 248), (38, 238, 255, 125), 2, (38, 238, 255, 40))
    text(d, (134, 665), "Future Self Paywall", 36, WHITE, "bold")
    text(d, (134, 718), "Shown before purchase. Digital premium features only.", 24, MUTED, "regular")
    draw_orbit_icon(base, (1015, 765), 82, CYAN, 7)

    draw_paywall_card(
        base,
        (134, 830),
        (468, 345),
        "Free",
        "GBP 0",
        "Start the identity loop.",
        ["3 daily missions", "Basic habit tracking", "Limited AI messages", "7-day timeline"],
        False,
    )
    draw_paywall_card(
        base,
        (640, 830),
        (468, 345),
        plan_title,
        price,
        subtitle,
        features,
        True,
    )

    rounded_rect(base, (92, 1228, 1150, 1858), 44, (17, 18, 45, 246), (255, 255, 255, 35), 2)
    text(d, (134, 1278), "Included with this subscription", 38, WHITE, "bold")
    feature_y = 1362
    for feature in features:
        rounded_rect(base, (134, feature_y - 10, 1108, feature_y + 70), 26, (255, 255, 255, 16), (255, 255, 255, 25))
        draw_check(d, 166, feature_y + 15, GREEN)
        wrapped_text(d, (214, feature_y + 6), feature, 28, 850, WHITE, "regular", 6)
        feature_y += 96

    rounded_rect(base, (92, 1928, 1150, 2168), 44, (28, 112, 255, 92), (38, 238, 255, 110), 2)
    text(d, (134, 1978), product_label, 34, WHITE, "bold")
    text(d, (134, 2032), product_id, 24, CYAN, "bold")
    wrapped_text(d, (134, 2080), "AI coaching is informational and motivational only. This app is not medical, mental health, therapy, diagnosis, legal, financial, or crisis support software.", 26, 950, (226, 235, 255), "regular", 8)

    rounded_rect(base, (180, 2260, 1062, 2360), 50, (38, 238, 255, 235), None, 1, (38, 238, 255, 70))
    text(d, (621, 2290), "Continue", 34, (3, 7, 18), "bold", anchor="ma")
    text(d, (621, 2422), "Manage or cancel anytime in Apple subscriptions.", 25, MUTED, "regular", anchor="ma")

    path.parent.mkdir(parents=True, exist_ok=True)
    base.convert("RGB").save(path, "PNG", optimize=True)


def generate_subscription_review_screenshots() -> None:
    products = [
        (
            "pro_monthly_review.png",
            "Pro Monthly",
            "GBP 9.99 / month",
            "Unlimited momentum.",
            [
                "Unlimited AI future-self messages",
                "Custom mission generation",
                "Advanced insights and XP trends",
                "Transformation timeline and widgets",
                "Premium share cards",
            ],
            "com.alteregoai.pro.monthly",
            "Alter Ego AI Pro Monthly",
        ),
        (
            "pro_yearly_review.png",
            "Pro Yearly",
            "GBP 79.99 / year",
            "A year of identity-based discipline.",
            [
                "Unlimited AI future-self messages",
                "Custom mission generation",
                "Advanced insights and weekly reviews",
                "Full transformation timeline",
                "Best value Pro access",
            ],
            "com.alteregoai.pro.yearly",
            "Alter Ego AI Pro Yearly",
        ),
        (
            "elite_monthly_review.png",
            "Elite Monthly",
            "GBP 19.99 / month",
            "The cinematic identity upgrade.",
            [
                "Advanced AI personalities",
                "Cinematic identity cards",
                "Deep weekly reviews",
                "Future-self voice placeholder",
                "Apple Watch placeholder and premium themes",
            ],
            "com.alteregoai.elite.monthly",
            "Alter Ego AI Elite Monthly",
        ),
    ]
    for filename, plan_title, price, subtitle, features, product_id, product_label in products:
        draw_subscription_review_screenshot(
            SUBSCRIPTION_REVIEW_ROOT / filename,
            plan_title,
            price,
            subtitle,
            features,
            product_id,
            product_label,
        )


def draw_subscription_promo_image(
    path: Path,
    tier: str,
    title: str,
    subtitle: str,
    price: str,
    product_id: str,
    accent: tuple[int, int, int],
) -> None:
    size = 1024
    image = gradient(
        (size, size),
        [(0, (4, 6, 20)), (0.48, (20, 17, 58)), (1, (3, 20, 35))],
    ).convert("RGBA")
    add_radial_glow(image, (780, 170), 560, accent, 145)
    add_radial_glow(image, (175, 850), 520, PURPLE, 135)
    d = ImageDraw.Draw(image)

    rounded_rect(image, (92, 92, 932, 932), 92, (255, 255, 255, 18), (*accent, 115), 3, (*accent, 58))
    rounded_rect(image, (132, 132, 892, 892), 72, (7, 10, 29, 210), (255, 255, 255, 30), 2)

    draw_orbit_icon(image, (512, 278), 130, accent, 9)
    text(d, (512, 445), "ALTER EGO AI", 36, accent, "bold", anchor="ma")
    wrapped_text(d, (190, 500), title, 76, 650, WHITE, "bold", 8)
    text(d, (512, 662), tier, 34, WHITE, "bold", anchor="ma")
    text(d, (512, 714), price, 30, accent, "bold", anchor="ma")

    rounded_rect(image, (222, 768, 802, 832), 32, (*accent, 38), (*accent, 125), 2)
    text(d, (512, 788), subtitle, 24, WHITE, "bold", anchor="ma")

    rounded_rect(image, (268, 846, 756, 888), 21, (*accent, 24), (*accent, 80))
    text(d, (512, 856), "PREMIUM DIGITAL ACCESS", 17, MUTED, "bold", anchor="ma")

    path.parent.mkdir(parents=True, exist_ok=True)
    image.convert("RGB").save(path, "PNG", optimize=True)


def generate_subscription_promo_images() -> None:
    products = [
        (
            "pro_monthly_image.png",
            "PRO MONTHLY",
            "Unlock Pro",
            "Unlimited future-self momentum",
            "GBP 9.99 / month",
            "com.alteregoai.pro.monthly",
            CYAN,
        ),
        (
            "pro_yearly_image.png",
            "PRO YEARLY",
            "A Year of Pro",
            "The full transformation timeline",
            "GBP 79.99 / year",
            "com.alteregoai.pro.yearly",
            BLUE,
        ),
        (
            "elite_monthly_image.png",
            "ELITE MONTHLY",
            "Apex Self Access",
            "Advanced identities and themes",
            "GBP 19.99 / month",
            "com.alteregoai.elite.monthly",
            GREEN,
        ),
    ]
    for filename, tier, title, subtitle, price, product_id, accent in products:
        draw_subscription_promo_image(
            SUBSCRIPTION_IMAGE_ROOT / filename,
            tier,
            title,
            subtitle,
            price,
            product_id,
            accent,
        )


def draw_screenshot(path: Path, size: tuple[int, int], headline: str, subhead: str, phone_title: str, screen_kind: str) -> None:
    w, h = size
    base = gradient(size, [(0, (3, 4, 16)), (0.42, (20, 17, 55)), (1, (2, 17, 34))]).convert("RGBA")
    add_radial_glow(base, (int(w * 0.82), int(h * 0.15)), int(w * 0.55), CYAN, 120)
    add_radial_glow(base, (int(w * 0.12), int(h * 0.88)), int(w * 0.6), PURPLE, 130)
    d = ImageDraw.Draw(base)
    text(d, (int(w * 0.08), int(h * 0.07)), "ALTER EGO AI", int(w * 0.033), CYAN, "bold")
    y = wrapped_text(d, (int(w * 0.08), int(h * 0.105)), headline, int(w * 0.075), int(w * 0.84), WHITE, "bold", int(w * 0.012))
    wrapped_text(d, (int(w * 0.08), y + int(w * 0.025)), subhead, int(w * 0.032), int(w * 0.82), (212, 222, 247), "regular", int(w * 0.01))

    phone_w = int(w * 0.76)
    phone_h = int(h * 0.62)
    phone_x = (w - phone_w) // 2
    phone_y = int(h * 0.335)
    draw_phone_frame(base, (phone_x, phone_y), (phone_w, phone_h), phone_title, screen_kind)
    path.parent.mkdir(parents=True, exist_ok=True)
    base.convert("RGB").save(path, "PNG", optimize=True)


def generate_screenshots() -> None:
    scenes = [
        ("01_future_self.png", "Become the person you were supposed to be.", "Create your Alter Ego identity and start a cinematic 7-day transformation plan.", "Future Identity", "onboarding"),
        ("02_daily_missions.png", "Daily missions that make discipline visible.", "Complete small actions, earn XP, protect your streak, and watch your identity evolve.", "Dashboard", "dashboard"),
        ("03_future_self_chat.png", "Talk to your future self.", "Get direct, motivational, non-medical coaching that turns excuses into practical next actions.", "Future Self Chat", "chat"),
        ("04_xp_insights.png", "Turn consistency into life XP.", "Track streaks, category strength, transformation score, and weekly progress with clean insights.", "Insights", "insights"),
        ("05_share_cards.png", "Make progress feel worth sharing.", "Generate premium transformation cards and share only when you choose.", "Share Cards", "share"),
    ]
    sizes = {
        "iPhone-6.5": (1242, 2688),
        "iPad-12.9": (2048, 2732),
    }
    for device, size in sizes.items():
        for filename, headline, subhead, phone_title, kind in scenes:
            draw_screenshot(SCREENSHOT_ROOT / device / filename, size, headline, subhead, phone_title, kind)


def generate_icon_image(size: int) -> Image.Image:
    image = gradient((size, size), [(0, (5, 7, 23)), (0.55, (20, 15, 66)), (1, (4, 23, 45))]).convert("RGBA")
    add_radial_glow(image, (int(size * 0.74), int(size * 0.22)), int(size * 0.58), CYAN, 140)
    add_radial_glow(image, (int(size * 0.18), int(size * 0.78)), int(size * 0.58), PURPLE, 135)
    d = ImageDraw.Draw(image)
    margin = int(size * 0.11)
    rounded_rect(image, (margin, margin, size - margin, size - margin), int(size * 0.18), (255, 255, 255, 18), (38, 238, 255, 95), max(1, size // 110))
    cx, cy = size // 2, int(size * 0.49)
    draw_orbit_icon(image, (cx, cy), int(size * 0.25), CYAN, max(2, size // 65))
    d.line((int(size * 0.34), int(size * 0.72), int(size * 0.48), int(size * 0.57), int(size * 0.60), int(size * 0.69), int(size * 0.73), int(size * 0.49)), fill=GREEN, width=max(3, size // 42), joint="curve")
    d.ellipse((cx - size * 0.085, cy - size * 0.085, cx + size * 0.085, cy + size * 0.085), fill=(245, 250, 255, 235))
    return image.convert("RGB")


def app_icon_entries() -> list[dict[str, str]]:
    specs = [
        ("20x20", "iphone", "2x", 40),
        ("20x20", "iphone", "3x", 60),
        ("29x29", "iphone", "2x", 58),
        ("29x29", "iphone", "3x", 87),
        ("40x40", "iphone", "2x", 80),
        ("40x40", "iphone", "3x", 120),
        ("60x60", "iphone", "2x", 120),
        ("60x60", "iphone", "3x", 180),
        ("20x20", "ipad", "1x", 20),
        ("20x20", "ipad", "2x", 40),
        ("29x29", "ipad", "1x", 29),
        ("29x29", "ipad", "2x", 58),
        ("40x40", "ipad", "1x", 40),
        ("40x40", "ipad", "2x", 80),
        ("76x76", "ipad", "1x", 76),
        ("76x76", "ipad", "2x", 152),
        ("83.5x83.5", "ipad", "2x", 167),
        ("1024x1024", "ios-marketing", "1x", 1024),
    ]
    entries = []
    for idiom, (size_label, platform, scale, pixels) in enumerate((s[0], s[1], s[2], s[3]) for s in specs):
        filename = f"app-icon-{platform}-{size_label.replace('.', '_')}@{scale}.png"
        entries.append({"size": size_label, "idiom": platform, "filename": filename, "scale": scale, "pixels": str(pixels)})
    return entries


def generate_app_icons() -> None:
    APPICONSET_ROOT.mkdir(parents=True, exist_ok=True)
    ICON_EXPORT_ROOT.mkdir(parents=True, exist_ok=True)
    base = generate_icon_image(1024)
    base.save(ICON_EXPORT_ROOT / "AlterEgoAI-AppIcon-1024.png", "PNG", optimize=True)

    images_json = []
    for entry in app_icon_entries():
        pixels = int(entry.pop("pixels"))
        icon = base.resize((pixels, pixels), Image.Resampling.LANCZOS)
        icon.save(APPICONSET_ROOT / entry["filename"], "PNG", optimize=True)
        images_json.append(entry)

    (XCASSETS_ROOT / "Contents.json").write_text(json.dumps({"info": {"author": "xcode", "version": 1}}, indent=2) + "\n", encoding="utf-8")
    (APPICONSET_ROOT / "Contents.json").write_text(json.dumps({"images": images_json, "info": {"author": "xcode", "version": 1}}, indent=2) + "\n", encoding="utf-8")

    ACCENT_ROOT.mkdir(parents=True, exist_ok=True)
    accent_json = {
        "colors": [
            {
                "idiom": "universal",
                "color": {
                    "color-space": "srgb",
                    "components": {"red": "0.149", "green": "0.933", "blue": "1.000", "alpha": "1.000"},
                },
            }
        ],
        "info": {"author": "xcode", "version": 1},
    }
    (ACCENT_ROOT / "Contents.json").write_text(json.dumps(accent_json, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    generate_screenshots()
    generate_app_icons()
    generate_subscription_review_screenshots()
    generate_subscription_promo_images()
    print(f"Generated assets under {ASSET_ROOT}")
    print(f"Generated Xcode asset catalog under {XCASSETS_ROOT}")


if __name__ == "__main__":
    main()
