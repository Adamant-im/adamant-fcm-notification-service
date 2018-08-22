package providers;


import com.google.inject.Provider;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import core.encryption.Encryptor;

public class EncryptorProvider implements Provider<Encryptor> {
    @Override
    public Encryptor get() {
        SodiumJava sodiumJava = new SodiumJava();
        LazySodiumJava lazySodiumJava = new LazySodiumJava(sodiumJava);

        return new Encryptor(lazySodiumJava);
    }
}
