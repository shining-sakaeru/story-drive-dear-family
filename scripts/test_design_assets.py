"""Resource acceptance for the supplied reference-based IVI redesign."""
import unittest
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
class DesignAssetsTest(unittest.TestCase):
    def test_native_gallery_metadata_has_all_referenced_images(self):
        gallery=ROOT/'docs/design/native-captures'
        for metadata_name in ['metadata.json','fontscale-1_5-metadata.json']:
            metadata=json.loads((gallery/metadata_name).read_text())
            for capture in metadata['captures']:
                for file_key,bounds_key in [('rootFile','rootBounds'),('themeFile','themeBounds')]:
                    name=capture[file_key]
                    self.assertEqual(Path(name).name,name)
                    path=gallery/name
                    self.assertTrue(path.is_file(),str(path))
                    bounds=capture[bounds_key]
                    with Image.open(path) as image:
                        self.assertEqual(image.size,(int(bounds['width']),int(bounds['height'])))

    def test_car_exterior_corners_are_fully_transparent(self):
        with Image.open(ROOT/'app/src/main/res/drawable-nodpi/img_genesis_car_base.webp') as im:
            for point in [(0,0),(im.width-1,0),(0,im.height-1),(im.width-1,im.height-1)]:
                self.assertEqual(im.getpixel(point)[3],0)

    def test_required_reference_bitmaps_have_valid_pixels(self):
        for name in ['genesis_car_base','genesis_seat_glow','warm_fox','warm_cabin','warm_seat','spatial_album']:
            with self.subTest(name=name):
                p=ROOT/'app/src/main/res/drawable-nodpi'/('img_'+name+'.webp')
                self.assertTrue(p.is_file(), str(p))
                with Image.open(p) as im:
                    self.assertGreater(min(im.size),64)
                    if 'genesis' in name:
                        self.assertEqual(im.mode,'RGBA')
                        low,high=im.getchannel('A').getextrema()
                        self.assertEqual(low,0)
                        self.assertGreater(high,100)
                    if name=='spatial_album':self.assertEqual(im.width,im.height)
    def test_spatial_vectors_parse(self):
        for name in ['seat_ventilation','seat_heating','fan','quick_mute','home','hvac','media','navigation']:
            p=ROOT/'app/src/main/res/drawable'/('ic_spatial_'+name+'.xml')
            self.assertTrue(p.is_file(),str(p))
            self.assertEqual(ET.parse(p).getroot().tag,'vector')
    def test_visual_docs_do_not_claim_unmeasured_similarity(self):
        content=(ROOT/'docs/design/README.md').read_text()
        for token in ['95%','10MB 미만','500MB+','래스터 비트맵 의존성을 배제']:
            self.assertNotIn(token,content)
        self.assertIn('Single Source of Truth',content)
        concepts=(ROOT/'docs/design/ui-redesign-concepts.md').read_text()
        self.assertIn('야간 오두막 일러스트 중심의 Family Harmony 카드',concepts)
        self.assertNotIn('원형 앰비언트 아크',concepts)
if __name__=='__main__':unittest.main()
