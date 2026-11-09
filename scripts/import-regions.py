import argparse
import csv
from datetime import date
from pathlib import Path
from zipfile import ZipFile


# CP949 고정 폭 원본에서 활성 읍·면·동과 전국 범위 확인
def read_regions(archive):
    with ZipFile(archive) as bundle:
        name, = (
            item for item in bundle.namelist()
            if Path(item).name.upper().startswith("KIKCD_H.")
            and not item.lower().endswith(".xlsx")
        )
        lines = bundle.read(name).splitlines()

    if not lines or not lines[0].decode("cp949").startswith("행정동코드"):
        raise ValueError("행정안전부 행정기관코드 원본인지 확인하세요.")

    regions = {}
    source_provinces = set()
    for line in lines[1:]:
        if len(line) != 300:
            raise ValueError("행정동 원본 레이아웃이 변경되었습니다.")
        code = line[0:10].decode("ascii")
        province = line[11:42].decode("cp949").strip()
        district = line[42:73].decode("cp949").strip()
        town = line[73:104].decode("cp949").strip()
        retired = line[113:121].strip()
        if not retired and province and code.endswith("00000000"):
            source_provinces.add(province)
        if retired or not town.endswith(("읍", "면", "동")):
            continue
        if not code.isdigit() or not province or code in regions:
            raise ValueError(f"잘못되거나 중복된 행정동 코드: {code}")
        regions[code] = (province, district or "-", town)
    if not source_provinces or source_provinces != {province for province, _, _ in regions.values()}:
        raise ValueError("원본의 일부 시·도에서 읍·면·동이 누락되었습니다.")
    return regions


parser = argparse.ArgumentParser()
parser.add_argument("archive", type=Path)
parser.add_argument("as_of", type=date.fromisoformat)
args = parser.parse_args()
regions = read_regions(args.archive)
target = Path("src/main/resources/regions/neighborhoods.tsv")
target.parent.mkdir(parents=True, exist_ok=True)
with target.open("w", encoding="utf-8", newline="") as stream:
    stream.write(f"# as-of={args.as_of.isoformat()}\n")
    writer = csv.writer(stream, delimiter="\t", lineterminator="\n")
    writer.writerows((code, *values) for code, values in sorted(regions.items()))
print(f"{target}: {len(regions)}개 행정동")