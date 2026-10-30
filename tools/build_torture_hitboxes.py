"""Bake per-cube hitboxes from MBO geometry, in metadata-0 world coordinates.

LastDawn's DecorShapes uses the same union-of-parts approach. Use MBO's own
geometry here: its offsets differ, especially the stocks and witch's chair.
No client/Gecko classes are needed to load the resulting shapes on a server.
"""
import itertools
import json
import math
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
REGISTRY = ROOT / 'src/main/java/ru/givler/mbo/registry/ModelRegistry.java'
# These already have specialized shapes/attachment behavior, not a solid model envelope.
SPECIAL = {'iron_cage', 'bed_on_chains', 'hook', 'hanged_corpse',
           'suspended_corpse', 'rope_around_the_neck', 'rope_core'}


def transform(point, part):
    """Gecko mirrors X and applies Rz * Ry * Rx around the mirrored pivot."""
    pivot = part.get('pivot', [0, 0, 0])
    pivot = [-pivot[0], pivot[1], pivot[2]]
    x, y, z = [point[i] - pivot[i] for i in range(3)]
    rx, ry, rz = part.get('rotation', [0, 0, 0])
    for axis, angle in enumerate((-rx, -ry, rz)):
        c, s = math.cos(math.radians(angle)), math.sin(math.radians(angle))
        if axis == 0:
            y, z = c*y-s*z, s*y+c*z
        elif axis == 1:
            x, z = c*x+s*z, -s*x+c*z
        else:
            x, y = c*x-s*y, s*x+c*y
    return [x+pivot[0], y+pivot[1], z+pivot[2]]


def shape(name, offset):
    path = ROOT / 'src/main/resources/assets/mbo/geo' / (name + '.geo.json')
    bones = json.loads(path.read_text(encoding='utf-8'))['minecraft:geometry'][0]['bones']
    by_name = {bone['name']: bone for bone in bones}
    boxes = []
    for bone in bones:
        if bone.get('neverRender'):
            continue
        for index, cube in enumerate(bone.get('cubes', [])):
            # The torture horse's head (6,7) and spike planes (11..14)
            # are decorative; retain only the wooden frame and legs.
            if name == 'death_horse' and bone['name'] == 'group_2' and index in {6, 7, 11, 12, 13, 14}:
                continue
            # Decorative flat surfaces remain selectable, but do not acquire a
            # whole block of collision. Gecko gives zero-width cubes a small inflate.
            inflate = cube.get('inflate', bone.get('inflate', 0))
            if not inflate and 0 in cube['size']:
                inflate = .016
            points = []
            for corner in itertools.product((0, 1), repeat=3):
                point = [cube['origin'][i] + (cube['size'][i]+inflate if corner[i] else -inflate)
                         for i in range(3)]
                point[0] = -point[0]
                point = transform(point, cube)
                parent = bone
                while parent is not None:
                    point = transform(point, parent)
                    parent = by_name.get(parent.get('parent'))
                # TemplateModelRenderer: metadata 0 faces SOUTH (180 degrees).
                recenter = 1 if name == 'hourglass' else 0
                points.append([.5-point[0]/16+recenter, point[1]/16+offset,
                               .5-point[2]/16+recenter])
            box = [min(p[i] for p in points) for i in range(3)]
            box += [max(p[i] for p in points) for i in range(3)]
            box = [round(v, 6) for v in box]
            # These ground decorations have inflated/rotated geometry below the
            # floor. It must not reserve helper cells inside the supporting dirt.
            if name in {'grave', 'embalmed_corpse'}:
                box[1] = max(0, box[1])
            # Keep the grave mound's collision level with its flat ground layer.
            if name == 'grave' and bone['name'] == 'group2' and index == 0:
                box[4] = .1 / 16
            # Keep floor decals from reserving a helper in the supporting block.
            if -.002 <= box[1] < 0:
                box[1] = 0
            if all(box[i] < box[i+3] for i in range(3)):
                boxes.append(box)
    # Duplicate body/clothing cubes and contained cubes add no collision surface.
    unique = sorted(set(tuple(b) for b in boxes))
    return [b for b in unique if not any(b != other and all(
        other[i] <= b[i] and other[i+3] >= b[i+3] for i in range(3)) for other in unique)]


def main():
    source = REGISTRY.read_text(encoding='utf-8')
    registrations = re.findall(r'(ModelTorture\w+) = new BlockModels\([^\n]+"instruments_of_torture/([^"/]+)"\);', source)
    total = 0
    for field, name in registrations:
        if sys.argv[1:] and name not in sys.argv[1:]:
            continue
        if name in SPECIAL or re.fullmatch(r'rope(?:_[1-5])?', name):
            continue
        offset_match = re.search(r'\b'+field+r'\.withModelOffsetY\(([-\d.]+)F\)', source)
        boxes = shape(name, float(offset_match[1]) if offset_match else 0)
        assert any(all(b[i] < 1 and b[i+3] > 0 for i in range(3)) for b in boxes), name
        lines = [field+'.withMultipartCollisionBoxes(new float[][] {']
        for box in boxes:
            lines.append('            {'+', '.join(f'{v:.6f}F' for v in box)+'},')
        lines.append('        });')
        replacement = '\n'.join(lines)
        source, count = re.subn(field+r'\.with(?:MultipartBounds|RotatingBounds)\([^;]+\);|'+field+r'\.withMultipartCollisionBoxes\([^;]+\);', replacement, source)
        assert count == 1, (field, count)
        total += 1
        print(f'{name}: {len(boxes)} boxes')
    REGISTRY.write_text(source, encoding='utf-8')
    print(f'Updated {total} model shapes.')


if __name__ == '__main__':
    main()
