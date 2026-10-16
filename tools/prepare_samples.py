"""Package original generated art and reproducible local sample media.
Requires Pillow and imageio-ffmpeg. Does not access any reference APK content.
"""
from pathlib import Path
import subprocess
import math
import struct
import wave
from PIL import Image
import imageio_ffmpeg

ROOT = Path(__file__).resolve().parents[1]
SAMPLES = ROOT / 'app/src/main/assets/samples'
SAMPLES.mkdir(parents=True, exist_ok=True)
ART = ROOT / 'store/source-art'
ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()

def run(*args):
    subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-y', *args], check=True)

# Size normalization for store upload/Android density buckets; retain source masters.
icon = Image.open(ROOT / 'store/icon/icon-master.png').convert('RGB')
icon.resize((512,512),Image.Resampling.LANCZOS).save(ROOT / 'store/icon/icon-512.png')
for density,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    directory=ROOT / f'app/src/main/res/mipmap-{density}'
    directory.mkdir(parents=True,exist_ok=True)
    icon.resize((size,size),Image.Resampling.LANCZOS).save(directory/'ic_launcher.png')

photo=ART/'coast-master.png'
Image.open(photo).convert('RGB').save(SAMPLES/'Coast_Still.jpg',quality=93)
run('-loop','1','-i',str(photo),'-f','lavfi','-i','sine=frequency=220:sample_rate=44100',
    '-vf','scale=1280:720','-t','8','-r','24','-c:v','libx264','-preset','ultrafast',
    '-pix_fmt','yuv420p','-g','24','-c:a','aac','-af','volume=0.04','-movflags','+faststart',str(SAMPLES/'Coast_01.mp4'))
run('-f','lavfi','-i','gradients=size=1280x720:rate=24:c0=0x83e2cb:c1=0x293851:c2=0x8f88de:c3=0x102735:duration=8',
    '-t','8','-c:v','libx264','-preset','ultrafast','-pix_fmt','yuv420p','-g','24',str(SAMPLES/'Motion_Study.mp4'))
with wave.open(str(SAMPLES/'Studio_Ambience.wav'),'wb') as output:
    rate=22050
    output.setnchannels(1);output.setsampwidth(2);output.setframerate(rate)
    output.writeframes(b''.join(struct.pack('<h',int(1700*math.sin(2*math.pi*220*i/rate)*min(i/rate,1)*min(8-i/rate,1))) for i in range(rate*8)))
print('Packaged icon sizes and four original sample media files.')
