"""Validate actual delivery constraints and write a manifest. Requires Pillow."""
from pathlib import Path
import hashlib
import json
from PIL import Image, ImageDraw, ImageOps

root=Path(__file__).resolve().parents[1]
store=root/'store'
records=[]

def inspect(path, size, limit):
    with Image.open(path) as image:
        assert image.format in ('PNG','JPEG'), f'Invalid format: {path}'
        assert image.size==size, f'Invalid dimensions: {path} {image.size}'
        assert path.stat().st_size<=limit, f'Oversized file: {path}'
        records.append({'path':path.relative_to(root).as_posix(),'width':image.width,'height':image.height,'bytes':path.stat().st_size,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})

inspect(store/'icon/icon-512.png',(512,512),1024*1024)
with Image.open(store/'icon/icon-512.png') as icon:
    assert icon.mode=='RGB','Store icon must be opaque RGB'
inspect(store/'feature-graphic.png',(1024,500),15*1024*1024)
for group in ('phone','desktop','tablet-7','tablet-10'):
    images=sorted((store/'screenshots'/group).glob('*.png'))
    assert len(images)==4,f'Expected four captures: {group}'
    for path in images:
        inspect(path,(1080,1920) if group=='phone' else (1920,1080),8*1024*1024)
    assert len({hashlib.sha256(p.read_bytes()).hexdigest() for p in images})==4,f'Duplicate screens: {group}'
for name,limit in [('short-description',80),('full-description',4000)]:
    value=(store/'listing'/f'{name}.txt').read_text(encoding='utf-8').strip()
    assert 0<len(value)<=limit,f'Description too long: {name}'
(store/'asset-manifest.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8')
# Review sheet only; deliverable screenshots above remain untouched.
sheet=Image.new('RGB',(1600,1160),'#18212e')
draw=ImageDraw.Draw(sheet)
for row,group in enumerate(('phone','desktop','tablet-7','tablet-10')):
    for col,path in enumerate(sorted((store/'screenshots'/group).glob('*.png'))):
        with Image.open(path) as screen:
            thumbnail=ImageOps.contain(screen,(390,255))
            x=col*400+(400-thumbnail.width)//2;y=row*290+25
            sheet.paste(thumbnail,(x,y))
            draw.text((col*400+10,row*290+5),group+' / '+path.stem,fill='white')
(root/'.local').mkdir(exist_ok=True)
sheet.save(root/'.local/screenshot-review.jpg',quality=90)
print(f'PASS: {len(records)} image assets; all image formats, dimensions, byte limits and listing lengths valid.')
