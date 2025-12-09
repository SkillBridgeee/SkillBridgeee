#!/usr/bin/env node

/**
 * Quick test script to verify Cloud Functions syntax
 * Run with: node test-functions-syntax.js
 */

const path = require('path');

console.log('🔍 Testing Cloud Functions syntax...\n');

try {
  // Try to load the functions
  const functionsPath = path.join(__dirname, 'index.js');
  console.log(`📂 Loading functions from: ${functionsPath}`);

  const functions = require('./index.js');

  console.log('\n✅ Functions loaded successfully!');
  console.log('\n📋 Exported functions:');

  const exportedFunctions = Object.keys(functions);
  exportedFunctions.forEach((name, index) => {
    console.log(`   ${index + 1}. ${name}`);
  });

  if (exportedFunctions.length === 0) {
    console.log('   ⚠️  No functions exported');
  }

  console.log('\n✨ All checks passed!\n');
  process.exit(0);

} catch (error) {
  console.error('\n❌ Error loading functions:');
  console.error(error.message);
  console.error('\nStack trace:');
  console.error(error.stack);
  console.log('\n💡 Make sure to run "npm install" first\n');
  process.exit(1);
}

