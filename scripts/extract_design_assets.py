"""Reproducible reference crops + alpha masks. No upscaling or invented car pixels."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageChops
import hashlib,json,math,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
BITMAP=ROOT/'app/src/main/res/drawable-nodpi'
VECTOR=ROOT/'app/src/main/res/drawable'
BITMAP.mkdir(parents=True,exist_ok=True);VECTOR.mkdir(parents=True,exist_ok=True)
SRC=ROOT/'docs/design/images'
paths=['ui_mockup_option1_genesis_ccnc.jpg','ui_mockup_option2_scandinavian_family_care.jpg','ui_mockup_option3_spatial_dynamic_widgets.jpg']
images=[Image.open(SRC/p).convert('RGB') for p in paths]
records=[]
def save(name,im,source=None,box=None,notes=None):
 p=BITMAP/(name+'.webp');im.save(p,'WEBP',lossless=True,quality=100,method=6)
 records.append({'path':p.relative_to(ROOT).as_posix(),'resolutionPx':list(im.size),'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'source':source,'sourceRectPx':box,'notes':notes})
box=(410,193,977,580)
car=images[0].crop(box).convert('RGBA')
# Bright vehicle/body pixels are preserved; dark scene background fades out.
bg=images[0].getpixel((414,196))
difference=ImageChops.difference(car.convert('RGB'),Image.new('RGB',car.size,bg))
brightness=ImageChops.lighter(ImageChops.lighter(difference.getchannel('R'),difference.getchannel('G')),difference.getchannel('B'))
def background_alpha(value):
 return round(min(255,max(0,value-5)*18))
alpha=brightness.point(background_alpha)
# Remove all overlapping source card lettering and their frames, not just glyphs.
mask=Image.new('L',car.size,255)
for b in [(257,168,501,285),(883,167,1129,345),(257,407,501,563),(883,407,1129,563)]:
 ImageDraw.Draw(mask).rectangle((b[0]-box[0],b[1]-box[1],b[2]-box[0],b[3]-box[1]),fill=0)
edge=Image.new('L',car.size)
# Foreground silhouette follows the supplied diagonal body/wheels, never redraws it.
ImageDraw.Draw(edge).polygon([(92,10),(126,1),(190,12),(210,25),(263,19),(331,52),(397,96),(463,145),(509,187),(554,227),(553,271),(512,316),(455,346),(387,369),(313,386),(280,371),(250,330),(205,293),(138,251),(101,217),(63,194),(32,181),(15,148),(9,109),(20,89),(60,60)],fill=255)
edge=edge.filter(ImageFilter.GaussianBlur(2))
ImageDraw.Draw(edge).rectangle((0,0,car.width-1,car.height-1),outline=0,width=2)
car.putalpha(ImageChops.multiply(ImageChops.multiply(alpha,mask),edge))
save('img_genesis_car_base',car,paths[0],box,'Source already has illuminated seats. Source card occlusions are transparent holes, not reconstructed vehicle detail. Layered original/hi-res render still required for lossless restoration.')
glow=Image.new('RGBA',(256,256));pixels=glow.load();assert pixels is not None
for y in range(256):
 for x in range(256):
  dx=(x-127.5)/127.5;dy=(y-127.5)/127.5;r=(dx*dx+dy*dy)**.5;t=x/255
  color=tuple(round(a*(1-t)+b*t) for a,b in zip((54,223,230),(255,190,92)))
  pixels[x,y]=color+(round(155*math.exp(-4*r*r)*max(0,1-r)),)
save('img_genesis_seat_glow',glow,notes='Generated soft cyan/amber radial alpha overlay; not a vehicle drawing.')
for name,i,b in [('img_warm_fox',1,(366,238,570,413)),('img_warm_cabin',1,(603,238,801,413)),('img_warm_seat',1,(859,252,1008,364)),('img_spatial_album',2,(739,217,852,330))]:
 save(name,images[i].crop(b),paths[i],b,'Literal supplied-reference crop at original pixel resolution; no high-resolution source is available.')
save('img_genesis_album',images[0].crop((897,221,975,301)),paths[0],(897,221,975,301),'Original album reference crop; not a streaming service or actual song playback.')
genesis_wave=images[0].crop((271,467,488,514)).convert('RGBA')
brightness=ImageChops.lighter(ImageChops.lighter(genesis_wave.getchannel('R'),genesis_wave.getchannel('G')),genesis_wave.getchannel('B'))
genesis_wave.putalpha(brightness.point(lambda value:min(255,max(0,value-65)*2)))
save('img_genesis_waveform',genesis_wave,paths[0],(271,467,488,514),'Reference mock waveform artwork; not measured audio.')
save('img_spatial_story',images[2].crop((585,419,653,488)),paths[2],(585,419,653,488),'Original galaxy story artwork; preserves detail instead of a primitive icon replacement.')
wave=images[2].crop((160,496,654,574)).convert('RGBA')
light=ImageChops.lighter(ImageChops.lighter(wave.getchannel('R'),wave.getchannel('G')),wave.getchannel('B'))
def wave_alpha(value):
 return min(255,max(0,value-55)*2)
wave.putalpha(light.point(wave_alpha))
save('img_spatial_waveform',wave,paths[2],(160,496,654,574),'Reference visualization, NOT a live audio measurement. Dark background extracted to alpha.')
curve=images[2].crop((984,545,1217,604)).convert('RGBA')
fade=Image.new('L',curve.size);ImageDraw.Draw(fade).rectangle((7,5,curve.width-8,curve.height-6),fill=255)
curve.putalpha(fade.filter(ImageFilter.GaussianBlur(5)))
save('img_spatial_comfort_curve',curve,paths[2],(984,545,1217,604),'Original comfort visualization sample; not measured AQI history.')
# Original clean line icons, drawn as Android VectorDrawable paths, not Compose Canvas.
icons={
 'seat_ventilation':'M5,3 L5,12 Q5,15 8,15 L15,15 Q17,15 17,17 L17,21 M8,4 L8,11 L17,11 M3,18 L14,18 M11,3 Q14,5 11,7 M16,3 Q19,5 16,7 M21,3 Q24,5 21,7',
 'seat_heating':'M4,4 L4,13 Q4,16 8,16 L16,16 L18,21 M7,4 L7,12 L17,12 M2,20 L14,20 M11,9 Q8,7 11,5 Q14,3 11,1 M16,9 Q13,7 16,5 Q19,3 16,1 M21,9 Q18,7 21,5 Q24,3 21,1',
 'fan':'M12,10 C8,3 18,1 17,7 C16,10 14,11 12,10 M14,12 C22,8 25,18 19,18 C16,18 14,15 14,12 M11,14 C15,22 5,24 5,18 C5,15 8,13 11,14 M10,11 C2,15 0,5 6,5 C9,5 11,8 10,11',
 'quick_mute':'M3,9 L7,9 L12,4 L12,20 L7,15 L3,15 Z M16,9 L22,15 M22,9 L16,15 M2,2 L22,22',
 'home':'M3,11 L12,3 L21,11 M5,10 L5,21 L10,21 L10,15 L14,15 L14,21 L19,21 L19,10',
 'hvac':'M3,7 L16,7 Q22,7 20,3 Q18,1 16,4 M3,12 L19,12 Q25,12 21,17 M3,17 L11,17 Q16,17 14,22',
 'media':'M5,3 L19,3 L19,21 L5,21 Z M10,8 L16,12 L10,16 Z',
 'navigation':'M3,10 L21,3 L14,21 L11,13 Z'}
for name,data in icons.items():
 p=VECTOR/('ic_spatial_'+name+'.xml')
 xml=f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24"><group android:scaleX="0.84" android:scaleY="0.84" android:translateX="1.5" android:translateY="1.5"><path android:pathData="{data}" android:fillColor="#00000000" android:strokeColor="#FFFFFFFF" android:strokeWidth="1.7" android:strokeLineCap="round" android:strokeLineJoin="round"/></group></vector>\n'
 ET.fromstring(xml);p.write_text(xml)
 records.append({'path':p.relative_to(ROOT).as_posix(),'resolutionPx':[24,24],'vectorViewport':True,'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
out=ROOT/'docs/design/asset-extraction-manifest.json'
out.write_text(json.dumps({'sourceImagesAreVisualTruth':True,'assets':records,'limitations':['Raster crops retain JPEG source resolution and source lighting.','Card-occluded parts of the car cannot be recovered from this JPG.','Reference usage rights/provenance must be checked before public distribution.']},ensure_ascii=False,indent=2)+'\n')
for a in records:print(f"{a['path']} | {a['resolutionPx'][0]}x{a['resolutionPx'][1]} {'vector viewport' if a.get('vectorViewport') else 'px'} | {a['bytes']/1024:.2f} KiB")
print('TOTAL_ASSETS',len(records))
