from datetime import datetime
import sys
import os

path = str(sys.argv[1])
head, tail = os.path.split(path)
tag = str(f"_{sys.argv[2]}" if len(sys.argv) >= 3 else "")

def make_datestring():
    return str(datetime.today().strftime('%Y%m%d_%H%M%S'))

datestring = make_datestring()
new_path = os.path.join(head, f"{datestring}{tag}_{tail}")

os.rename(path, new_path)
