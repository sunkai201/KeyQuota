from PIL import Image, ImageDraw, ImageFont
import os

RES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "app", "src", "main", "res")
SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
FONT_BLACK = "C:/Windows/Fonts/ariblk.ttf"
FONT_MONO = "C:/Windows/Fonts/consola.ttf"


def make(size):
    s = size * 8
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    r = int(s * 0.23)
    d.rounded_rectangle([0, 0, s - 1, s - 1], radius=r, fill=(18, 11, 24, 255))
    pad = int(s * 0.13)
    key_r = int(s * 0.13)
    d.rounded_rectangle([pad, pad, s - pad, s - pad], radius=key_r,
                        fill=(181, 36, 70, 255), outline=(255, 202, 148, 255),
                        width=max(2, s // 40))
    font = ImageFont.truetype(FONT_BLACK, int(s * 0.50))
    bbox = d.textbbox((0, 0), "K", font=font)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    d.text(((s - tw) / 2 - bbox[0], (s - th) / 2 - bbox[1] - s * 0.03), "K",
           font=font, fill=(255, 237, 171, 255))
    mono = ImageFont.truetype(FONT_MONO, int(s * 0.12))
    d.text((s / 2, s * 0.79), "108", font=mono, fill=(255, 210, 106, 255), anchor="mm")
    return img.resize((size, size), Image.LANCZOS)


for density, px in SIZES.items():
    folder = os.path.join(RES, "mipmap-" + density)
    os.makedirs(folder, exist_ok=True)
    path = os.path.join(folder, "ic_launcher.png")
    make(px).save(path)
    print("wrote", path)
