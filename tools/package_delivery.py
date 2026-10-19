"""Package verified local release artifacts; never includes signing secrets or reference APK."""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

root=Path(__file__).resolve().parents[1]
destination=root/'deliverables'
destination.mkdir(exist_ok=True)
for source,target in [
    ('app/build/outputs/apk/release/app-release.apk','CineLayer-1.0.0.apk'),
    ('app/build/outputs/bundle/release/app-release.aab','CineLayer-1.0.0.aab')]:
    shutil.copy2(root/source,destination/target)
assets=[root/'store/icon/icon-512.png',root/'store/feature-graphic.png',root/'index.html']
assets+=list((root/'store/listing').glob('*.txt'))
assets+=list((root/'store/screenshots').glob('*/*.png'))
assets+=list((root/'store/translations').glob('*'))
assets+=list((root/'store/releases').glob('*/*.txt'))
assets+=[root/'store'/name for name in ['asset-manifest.json','UPLOAD.md','compliance-notes.md','artwork-provenance.md']]
with zipfile.ZipFile(destination/'CineLayer-Play-Store-Assets.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(assets):archive.write(path,path.relative_to(root).as_posix())
with zipfile.ZipFile(destination/'CineLayer-Search-Evidence.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted((root/'analysis').rglob('*')):
        if path.is_file():archive.write(path,path.relative_to(root).as_posix())
checksums={p.name:{'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in destination.iterdir() if p.suffix in ('.apk','.aab','.zip')}
(destination/'checksums.json').write_text(json.dumps(checksums,indent=2)+'\n',encoding='utf-8')
print('Packaged signed APK/AAB, English Play Store assets, local search evidence, and checksums.')
