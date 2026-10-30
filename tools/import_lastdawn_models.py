"""Import the reviewed LastDawn stocks/chair into MBO's Gecko format.

Usage: python tools/import_lastdawn_models.py E:/Ariamis_src/LastDawn
Preserves visible elements, per-face UVs, pivots and the model placement offset.
"""
import importlib.util
import itertools
import json
from pathlib import Path
import re
import shutil
import sys

from PIL import Image
from build_torture_hitboxes import transform

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/mbo'
MODELS = {'witch_s_chair': 'ModelTortureWitchSChair',
          'sharpened_in_the_pads': 'ModelTortureSharpenedInThePads'}


def main():
    dawn = Path(sys.argv[1]).resolve()
    source = dawn / 'src/main/resources/assets/lastdawn'
    # Share the reference exporter's face/UV conventions, including flipped UVs.
    spec = importlib.util.spec_from_file_location('dawn_export', dawn / 'tools/convert_static_models.py')
    uv_tools = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(uv_tools)
    registry = ROOT / 'src/main/java/ru/givler/mbo/registry/ModelRegistry.java'
    registrations = registry.read_text(encoding='utf-8')
    for name, field in MODELS.items():
        model = json.loads((source / 'models/block' / (name+'.json')).read_text(encoding='utf-8'))
        elements = [e for e in model['lastdawn_elements'] if e.get('render', True)]
        textures = {f['texture'][1:] for e in elements for f in e['faces'].values()}
        assert len(textures) == 1, 'Visible geometry requires an atlas if multiple textures are used'
        texture = source / 'textures' / (model['textures'][textures.pop()].split(':')[1]+'.png')
        width, height = Image.open(texture).size
        offset = model.get('lastdawn_model_offset', [0, 0, 0])
        assert not model.get('lastdawn_scale') and not model.get('lastdawn_ground_lift')
        cubes = []
        envelope = []
        for element in elements:
            lo, hi = element['from'], element['to']
            transforms = element.get('transforms', [])
            assert len(transforms) <= 1, 'Compound rotations require decomposition'
            rotation = transforms[0]['rotation'] if transforms else [0, 0, 0]
            assert sum(abs(a) > 1e-8 for a in rotation) <= 1
            assert 'transform' not in element and 'rotation' not in element
            # Gecko mirrors X; MBO metadata 0 adds a half turn about Y.
            cube = {'origin': [lo[0]+offset[0]-8, lo[1]+offset[1], 8-hi[2]-offset[2]],
                    'size': [hi[i]-lo[i] for i in range(3)], 'uv': {}}
            if transforms:
                pivot = transforms[0]['origin']
                cube['pivot'] = [pivot[0]+offset[0]-8, pivot[1]+offset[1], 8-pivot[2]-offset[2]]
                cube['rotation'] = [rotation[0], -rotation[1], -rotation[2]]
            q = [[-1, 0, 0], [0, 1, 0], [0, 0, -1]]
            corners = [uv_tools.mv(q, p) for p in uv_tools.corners(lo, hi)]
            new_lo = [min(p[i] for p in corners) for i in range(3)]
            new_hi = [max(p[i] for p in corners) for i in range(3)]
            for face, data in element['faces'].items():
                if len({tuple(p) for p in uv_tools.corners(lo, hi, face)}) < 4:
                    continue
                u0, v0, u1, v1 = data['uv']
                pixels = [u0*width/16, v0*height/16, u1*width/16, v1*height/16]
                mapped, uv = uv_tools.remap_face(face, pixels, data.get('rotation', 0), lo, hi, new_lo, new_hi, q)
                coords = uv['uv']
                if mapped in ('up', 'down'):
                    coords = coords[2:] + coords[:2]
                cube['uv'][mapped] = {'uv': coords[:2],
                                      'uv_size': [coords[2]-coords[0], coords[3]-coords[1]]}
                if uv.get('rotation'):
                    cube['uv'][mapped]['uv_rotation'] = uv['rotation']
            # Independently replay Gecko's transform and check every corner
            # against the reference renderer's pivot rotation in Java coordinates.
            for corner in itertools.product((0, 1), repeat=3):
                expected = [(hi if corner[i] else lo)[i] for i in range(3)]
                if transforms:
                    pivot = transforms[0]['origin']
                    r = uv_tools.rotation('xyz'.index(next(axis for axis, a in zip('xyz', rotation) if a)),
                                          next(a for a in rotation if a))
                    expected = uv_tools.add(uv_tools.mv(r, uv_tools.sub(expected, pivot)), pivot)
                expected = uv_tools.add(expected, offset)
                raw = [lo[0]+(hi[0]-lo[0])*corner[0]+offset[0]-8,
                       lo[1]+(hi[1]-lo[1])*corner[1]+offset[1],
                       8-(lo[2]+(hi[2]-lo[2])*corner[2])-offset[2]]
                raw[0] = -raw[0]
                rendered = transform(raw, cube)
                actual = [8-rendered[0], rendered[1], 8-rendered[2]]
                assert uv_tools.close(expected, actual), (name, expected, actual)
                envelope.append(actual)
            cubes.append(cube)
        minimum = [min(p[i] for p in envelope)/16 for i in range(3)]
        maximum = [max(p[i] for p in envelope)/16 for i in range(3)]
        geo = {'format_version': '1.12.0', 'minecraft:geometry': [{
            'description': {'identifier': 'geometry.'+name, 'texture_width': width,
                'texture_height': height, 'visible_bounds_width': 4,
                'visible_bounds_height': 4, 'visible_bounds_offset': [0, 1, 0]},
            'bones': [{'name': 'lastdawn', 'pivot': [0, 0, 0], 'cubes': cubes}]}]}
        (ASSETS / 'geo' / (name+'.geo.json')).write_text(json.dumps(geo, indent=2)+'\n', encoding='utf-8')
        shutil.copyfile(texture, ASSETS / 'textures/models/decor' / (name+'.png'))
        registrations = re.sub(r'        '+field+r'\.withModelOffsetY\([^;]+\);\n', '', registrations)
        print(f'{name}: {len(cubes)} visible elements, {width}x{height}, verified all corners; bounds {minimum} .. {maximum}')
    registry.write_text(registrations, encoding='utf-8')


if __name__ == '__main__':
    main()
