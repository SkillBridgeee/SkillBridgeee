#!/usr/bin/env python3
import re

file_path = "app/src/androidTest/java/com/android/sample/screen/ListingScreenTest.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Replace all .performScrollTo().performClick() with just .performClick()
content = content.replace('.performScrollTo().performClick()', '.performClick()')

with open(file_path, 'w') as f:
    f.write(content)

print("Fixed all performScrollTo() calls")

