package com.dealshot.app.data.preferences
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.longPreferencesKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureTokenStorage(
    private val context: Context
) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "dealshot_auth_token_key"

        private const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        private const val GCM_TAG_LENGTH = 128

    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    private fun getOrCreateKey(): SecretKey {

        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    fun encrypt(token: String): String {

        if (token.isBlank()) {
            return ""
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encryptedBytes = cipher.doFinal(
            token.toByteArray(StandardCharsets.UTF_8)
        )

        val iv = cipher.iv

        val combined = ByteArray(
            iv.size + encryptedBytes.size
        )

        System.arraycopy(
            iv,
            0,
            combined,
            0,
            iv.size
        )

        System.arraycopy(
            encryptedBytes,
            0,
            combined,
            iv.size,
            encryptedBytes.size
        )

        return Base64.encodeToString(
            combined,
            Base64.NO_WRAP
        )
    }

    fun decrypt(encryptedToken: String): String? {

        if (encryptedToken.isBlank()) {
            return null
        }

        return try {

            val combined = Base64.decode(
                encryptedToken,
                Base64.NO_WRAP
            )

            val ivSize = 12

            val iv = combined.copyOfRange(
                0,
                ivSize
            )

            val encryptedBytes =
                combined.copyOfRange(
                    ivSize,
                    combined.size
                )

            val cipher = Cipher.getInstance(
                TRANSFORMATION
            )

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    GCM_TAG_LENGTH,
                    iv
                )
            )

            String(
                cipher.doFinal(encryptedBytes),
                StandardCharsets.UTF_8
            )

        } catch (e: Exception) {
            null
        }
    }

    fun clearKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (_: Exception) {
        }
    }
}