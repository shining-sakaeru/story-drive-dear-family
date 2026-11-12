import unittest,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
UI=ROOT/'app/src/main/java/com/pleos/dearfamily/ui'
class ContainerContractTest(unittest.TestCase):
    def test_story_controls_expose_actual_caption_text(self):
        for name in ['GenesisLuxuryCockpitView', 'WarmFamilyStoryView', 'SpatialDynamicCardsView']:
            with self.subTest(theme=name):
                source=(UI/f'components/{name}.kt').read_text()
                self.assertIn('state.flow.gleoTtsScript',source)
                self.assertIn('StoryCaption',source)

    def test_spatial_has_real_adjustable_vertical_control_and_dock_contracts(self):
        source=(UI/'components/SpatialDynamicCardsView.kt').read_text()
        for token in ['img_spatial_album','VerticalVentilation','setProgress','onTabSelect','onScenarioSelect','onVentilationChange']:
            self.assertIn(token,source)
        self.assertNotIn('onToggleFun',source)
        self.assertNotRegex(source,r'\b(Canvas|Path)\s*\(')
        self.assertNotRegex(source,r'height\(\s*(230|440)\.dp')

    def test_warm_artwork_and_care_intents_are_independent(self):
        source=(UI/'components/WarmFamilyStoryView.kt').read_text()
        for token in ['img_warm_fox','img_warm_cabin','img_warm_seat','onStoryPlayback','onCabinWarmth','onQuietCabin']:
            self.assertIn(token,source)
        self.assertNotIn('onToggleFun',source)
        self.assertNotRegex(source,r'\b(Canvas|Path)\s*\(')
        self.assertNotRegex(source,r'height\(\s*440\.dp')

    def test_genesis_uses_reference_layers_instead_of_vehicle_canvas(self):
        source=(UI/'components/GenesisLuxuryCockpitView.kt').read_text()
        self.assertIn('img_genesis_car_base',source)
        self.assertIn('img_genesis_seat_glow',source)
        self.assertIn('genesisVehicleArtwork',source)
        self.assertNotRegex(source,r'\b(Canvas|Path)\s*\(')
        self.assertNotRegex(source,r'height\(\s*(440|230)\.dp')

    def test_diagnostics_are_explicitly_opt_in(self):
        main=(UI/'DearFamilyMainScreen.kt').read_text()
        dock=(UI/'components/ScenarioInjectorDock.kt').read_text()
        self.assertIn('debugBadge',main)
        self.assertIn('if (showDebug)',main)
        self.assertIn('DebugOverlay',main)
        self.assertIn('initiallyExpanded: Boolean = false',dock)
    def test_common_fonts_are_at_least_fourteen_sp(self):
        for rel in ['DearFamilyMainScreen.kt','theme/DearFamilyTheme.kt']:
            for n in re.findall(r'fontSize\s*=\s*(\d+(?:\.\d+)?)\.sp\b',(UI/rel).read_text()):
                self.assertGreaterEqual(float(n),14,rel)
if __name__=='__main__':unittest.main()
