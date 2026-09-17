build-release:
	cargo build --release
	boltffi pack android --release
	cp -r ./dist/android/jniLibs ../app/src/main/
	cp ./dist/android/kotlin/starmage27/shumide/TreeSitterBridge.kt ../app/src/main/java/starmage27/shumide/TreeSitterBridge.kt
