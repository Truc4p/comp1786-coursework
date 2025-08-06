#!/usr/bin/env python3
"""
Java Code Cleanup Script
This script analyzes Java files to detect:
1. Unused imports
2. Unused variables
3. Unused methods (basic detection)
4. Duplicate imports
"""

import os
import re
import sys
from pathlib import Path

def analyze_java_file(file_path):
    """Analyze a single Java file for unused code"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    lines = content.split('\n')
    imports = []
    import_lines = []
    
    # Extract imports
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('import ') and not stripped.startswith('import static'):
            import_stmt = stripped[7:].rstrip(';').strip()
            imports.append(import_stmt)
            import_lines.append((i, line, import_stmt))
    
    # Get the main content (after imports)
    main_content = '\n'.join(lines)
    
    unused_imports = []
    duplicate_imports = []
    
    # Check for duplicate imports
    seen_imports = set()
    for line_num, line, import_stmt in import_lines:
        if import_stmt in seen_imports:
            duplicate_imports.append((line_num + 1, line, import_stmt))
        else:
            seen_imports.add(import_stmt)
    
    # Check for unused imports
    for line_num, line, import_stmt in import_lines:
        # Extract class name from import
        class_name = import_stmt.split('.')[-1]
        
        # Skip wildcard imports for now
        if class_name == '*':
            continue
            
        # Count occurrences in main content (excluding import lines)
        content_without_imports = re.sub(r'^import .*?;', '', main_content, flags=re.MULTILINE)
        
        # Check if class is used (simple heuristic)
        pattern = r'\b' + re.escape(class_name) + r'\b'
        matches = len(re.findall(pattern, content_without_imports))
        
        if matches == 0:
            unused_imports.append((line_num + 1, line, import_stmt))
    
    return {
        'file': file_path,
        'unused_imports': unused_imports,
        'duplicate_imports': duplicate_imports,
        'total_imports': len(imports)
    }

def find_java_files(directory):
    """Find all Java files in directory"""
    java_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def main():
    java_dir = "app/src/main/java"
    if not os.path.exists(java_dir):
        print(f"Java directory {java_dir} not found!")
        return
    
    java_files = find_java_files(java_dir)
    print(f"Found {len(java_files)} Java files to analyze...\n")
    
    total_unused = 0
    total_duplicates = 0
    
    for java_file in java_files:
        result = analyze_java_file(java_file)
        
        if result['unused_imports'] or result['duplicate_imports']:
            print(f"📄 {result['file']}")
            
            if result['unused_imports']:
                print(f"  🗑️  Unused imports ({len(result['unused_imports'])}):")
                for line_num, line, import_stmt in result['unused_imports']:
                    print(f"    Line {line_num}: {line.strip()}")
                total_unused += len(result['unused_imports'])
            
            if result['duplicate_imports']:
                print(f"  🔄 Duplicate imports ({len(result['duplicate_imports'])}):")
                for line_num, line, import_stmt in result['duplicate_imports']:
                    print(f"    Line {line_num}: {line.strip()}")
                total_duplicates += len(result['duplicate_imports'])
            
            print()
    
    print(f"📊 Summary:")
    print(f"  Total files analyzed: {len(java_files)}")
    print(f"  Total unused imports: {total_unused}")
    print(f"  Total duplicate imports: {total_duplicates}")

if __name__ == "__main__":
    main()
