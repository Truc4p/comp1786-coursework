#!/usr/bin/env python3
"""
Advanced Java Code Cleanup Script
Detects more complex unused code patterns:
1. Unused variables
2. Unused private methods
3. Empty catch blocks
4. Dead code patterns
"""

import os
import re
from pathlib import Path

def analyze_unused_variables(content):
    """Detect unused local variables"""
    unused_vars = []
    
    # Find variable declarations
    var_patterns = [
        r'(\w+)\s+(\w+)\s*=',  # Type varName =
        r'(\w+)\s+(\w+)\s*;',  # Type varName;
    ]
    
    for pattern in var_patterns:
        matches = re.finditer(pattern, content)
        for match in matches:
            var_type = match.group(1)
            var_name = match.group(2)
            
            # Skip common keywords and primitive types
            if var_type in ['if', 'for', 'while', 'switch', 'return', 'public', 'private', 'protected', 'static', 'final']:
                continue
            
            # Count usage of variable (excluding declaration)
            usage_pattern = r'\b' + re.escape(var_name) + r'\b'
            all_matches = re.findall(usage_pattern, content)
            
            # If variable is used only once (in declaration), it might be unused
            if len(all_matches) == 1:
                unused_vars.append(var_name)
    
    return unused_vars

def analyze_unused_methods(content):
    """Detect unused private methods"""
    unused_methods = []
    
    # Find private method declarations
    method_pattern = r'private\s+(?:static\s+)?(?:\w+\s+)*(\w+)\s*\([^)]*\)\s*\{'
    matches = re.finditer(method_pattern, content)
    
    for match in matches:
        method_name = match.group(1)
        
        # Skip constructors and common method names
        if method_name in ['onCreate', 'onDestroy', 'onPause', 'onResume', 'onStart', 'onStop']:
            continue
        
        # Count method calls
        call_pattern = r'\b' + re.escape(method_name) + r'\s*\('
        calls = re.findall(call_pattern, content)
        
        # If method is called only once (in declaration), it might be unused
        if len(calls) == 1:
            unused_methods.append(method_name)
    
    return unused_methods

def analyze_empty_blocks(content):
    """Detect empty catch blocks and other empty blocks"""
    empty_blocks = []
    
    # Find empty catch blocks
    catch_pattern = r'catch\s*\([^)]+\)\s*\{\s*\}'
    matches = re.finditer(catch_pattern, content)
    for match in matches:
        empty_blocks.append(f"Empty catch block: {match.group()}")
    
    # Find empty if blocks
    if_pattern = r'if\s*\([^)]+\)\s*\{\s*\}'
    matches = re.finditer(if_pattern, content)
    for match in matches:
        empty_blocks.append(f"Empty if block: {match.group()}")
    
    return empty_blocks

def analyze_dead_code(content):
    """Detect potential dead code patterns"""
    dead_code = []
    
    # Find unreachable code after return statements
    lines = content.split('\n')
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('return') and i + 1 < len(lines):
            next_line = lines[i + 1].strip()
            if next_line and not next_line.startswith('}') and not next_line.startswith('//'):
                dead_code.append(f"Line {i + 2}: Potential unreachable code after return")
    
    # Find if (false) blocks
    false_pattern = r'if\s*\(\s*false\s*\)'
    matches = re.finditer(false_pattern, content)
    for match in matches:
        dead_code.append(f"Dead code: {match.group()}")
    
    return dead_code

def analyze_java_file_advanced(file_path):
    """Advanced analysis of Java file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        return None
    
    return {
        'file': file_path,
        'unused_variables': analyze_unused_variables(content),
        'unused_methods': analyze_unused_methods(content),
        'empty_blocks': analyze_empty_blocks(content),
        'dead_code': analyze_dead_code(content)
    }

def main():
    java_dir = "app/src/main/java"
    if not os.path.exists(java_dir):
        print(f"Java directory {java_dir} not found!")
        return
    
    java_files = []
    for root, dirs, files in os.walk(java_dir):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    
    print(f"🔍 Advanced analysis of {len(java_files)} Java files...\n")
    
    total_issues = 0
    
    for java_file in java_files:
        result = analyze_java_file_advanced(java_file)
        if result is None:
            continue
        
        has_issues = any([
            result['unused_variables'],
            result['unused_methods'],
            result['empty_blocks'],
            result['dead_code']
        ])
        
        if has_issues:
            print(f"📄 {result['file']}")
            
            if result['unused_variables']:
                print(f"  🔶 Potential unused variables ({len(result['unused_variables'])}):")
                for var in result['unused_variables'][:5]:  # Show max 5
                    print(f"    - {var}")
                total_issues += len(result['unused_variables'])
            
            if result['unused_methods']:
                print(f"  🔶 Potential unused private methods ({len(result['unused_methods'])}):")
                for method in result['unused_methods']:
                    print(f"    - {method}()")
                total_issues += len(result['unused_methods'])
            
            if result['empty_blocks']:
                print(f"  ⚠️  Empty blocks ({len(result['empty_blocks'])}):")
                for block in result['empty_blocks']:
                    print(f"    - {block}")
                total_issues += len(result['empty_blocks'])
            
            if result['dead_code']:
                print(f"  💀 Potential dead code ({len(result['dead_code'])}):")
                for code in result['dead_code']:
                    print(f"    - {code}")
                total_issues += len(result['dead_code'])
            
            print()
    
    print(f"📊 Advanced Analysis Summary:")
    print(f"  Total files analyzed: {len(java_files)}")
    print(f"  Total potential issues found: {total_issues}")
    print(f"\n💡 Note: These are potential issues that may need manual review.")

if __name__ == "__main__":
    main()
