package core.encryption;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import core.entities.Transaction;
import core.entities.TransactionMessage;
import core.entities.TransactionState;
import io.github.novacrypto.bip39.SeedCalculator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Encryptor {
    private final int NONCE_LENGTH = 24;
    private LazySodium sodium;
    private SeedCalculator seedCalculator;

    public Encryptor(LazySodium sodium, SeedCalculator seedCalculator) {
        this.sodium = sodium;
        this.seedCalculator = seedCalculator;
    }

    public KeyPair getKeyPairFromPassPhrase(String passPhrase) {
        KeyPair pair = null;

        try {

            byte[] blankCalculatedSeed = seedCalculator.calculateSeed(passPhrase, "");
            String seedString = Hex.bytesToHex(blankCalculatedSeed);

            byte[] seedForHash = Hex.encodeStringToHexArray(seedString);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] seed = digest.digest(seedForHash);

            pair = sodium.cryptoSignSeedKeypair(seed);

        } catch (NoSuchAlgorithmException | SodiumException e) {
            Logger.getGlobal().severe(e.getMessage());
        }

        return pair;
    }

    public String decryptMessage(String message, String ownMessage, String senderPublicKey, String mySecretKey) {
        String decryptedMessage = "";

        byte[] nonceBytes = Hex.encodeStringToHexArray(ownMessage);

        try {
            KeyPair ed25519KeyPair = new KeyPair(Key.fromHexString(senderPublicKey), Key.fromHexString(mySecretKey));
            KeyPair curve25519KeyPair = sodium.convertKeyPairEd25519ToCurve25519(ed25519KeyPair);

            decryptedMessage = sodium.cryptoBoxOpenEasy(message, nonceBytes, curve25519KeyPair);
        } catch (SodiumException e) {
            e.printStackTrace();
        }

        return decryptedMessage;
    }

    public TransactionMessage encryptMessage(String message, String recipientPublicKey, String mySecretKey){
        TransactionMessage chat = null;
        try {

            byte[] nonceBytes = sodium.randomBytesBuf(NONCE_LENGTH);

            KeyPair ed25519KeyPair = new KeyPair(Key.fromHexString(recipientPublicKey), Key.fromHexString(mySecretKey));
            KeyPair curve25519KeyPair = sodium.convertKeyPairEd25519ToCurve25519(ed25519KeyPair);

            String ecryptedMessage = sodium.cryptoBoxEasy(message, nonceBytes, curve25519KeyPair);

            chat = new TransactionMessage();
            chat.setMessage(ecryptedMessage.toLowerCase());
            chat.setOwnMessage(Hex.bytesToHex(nonceBytes));

        }catch (SodiumException e){
            e.printStackTrace();
        }

        return chat;
    }



    public String createTransactionSignature(Transaction transaction, KeyPair keyPair) {
        String sign = "";
        byte[] transactionBytes = transaction.getBytesDigest();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(transactionBytes);

            byte[] signBytes = new byte[Sign.BYTES];

            sodium.getSodium().crypto_sign_detached(
                    signBytes,
                    null,
                    hash,
                    (long)hash.length,
                    keyPair.getSecretKey().getAsBytes()
            );

            sign = Hex.bytesToHex(signBytes);

        } catch ( NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sign;
    }

    public String generateRandomString() { ;
        return Long.toString((long)(Math.random() * 10_000_000_000_000L), 36)
                .replace("/[^a-z]+/g", "");
    }

    private byte[] createCryptoHashSha256FromPrivateKey(String mySecretKey) {
        mySecretKey = mySecretKey.toLowerCase();
        byte[] cryptoHashSha256 = new byte[0];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            cryptoHashSha256 = digest.digest(Hex.encodeStringToHexArray(mySecretKey));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return cryptoHashSha256;
    }
}
