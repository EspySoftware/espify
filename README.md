
# Compile and run

```bash
./mvnw clean install
java -jar target/espify-1.0.jar
    o
./start_terminal_client.bat
```

```bash
join-room test
add-song C:/Users/Fernando/Music/song.mp3
jar cfm target/TerminalClient.jar manifest.txt -C target/classes client -C target/classes utils -C target/libs jl1.0.1.jar
```
