# Overview
This project was created as an alternative to the Discord Rhythm bot. After encountering issues such as poor streaming quality with Rhythm, 
I decided to develop my own tool to play music via command line. 
Unlike Rhythm, this software downloads music from YouTube and plays it locally through a Command Line Interface (CLI), rather than streaming it, it uses yt-dlp under the hood for searching and downloading.
Once initialized, the app launches a subshell process where you can play your desired music and get complete control over the playback, including pausing, skipping, rewinding/forwarding and looping.

**NOTE: The project is still in early deployment phase and is only available for Windows for now because of testing purposes, if you think that the project is worth contributing and want to help by making it cross-platform or adding features, check contribution guidelines below**

# How to install
1. Check for the latest versions in the releases
2. Download the zip folder and extract
3. Run install.bat as administrator
4. Open command prompt or powershell and type `mpp init`, you should see something like:

<img width="1466" height="492" alt="image" src="https://github.com/user-attachments/assets/4218bed9-5e84-47e9-bdd1-a78ac2a9101d" />

# Cloning Instructions
**IMPORTANT, APPLIES TO BOTH SCENARIOS: After cloning make sure to download the binaries from bin.zip, unzip them and drop them in /src/main/**
- If you're using intellij to clone, make sure you have this config setup after cloning
<img width="989" height="852" alt="image" src="https://github.com/user-attachments/assets/f3949cda-eb3f-49b1-a7ae-f12a2cb66e4b" />

- If you're using another editor, make sure to run this command in the terminal:
`java --enable-native-access=ALL-UNNAMED -jar target/MusicPlayer-1.0-SNAPSHOT-jar-with-dependencies.jar init`

# Contributing
- Bug Reports & Feature Requests: Please open a GitHub Issue.

- Direct Contact: Reach out to me on Discord **Mr.Shut#1397** for a quick chat or via Gmail at **longurashvilin@gmail.com** for formal.

### Disclaimer:
This tool is intended strictly for personal use. Music is downloaded from YouTube, so users must ensure compliance with YouTube’s Terms of Service and all applicable copyright laws. 
The developer is not responsible for any misuse or distribution of downloaded content.
