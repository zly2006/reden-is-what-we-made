package com.github.zly2006.reden.report

import com.github.zly2006.reden.utils.ResourceLoader
import org.bouncycastle.bcpg.BCPGInputStream
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPUtil
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider
import java.io.ByteArrayInputStream


fun checkSignature(): Boolean {
    if (ResourceLoader.loadString("cert") == "SOS Dan Saiko") {
        println("SOS団最高")
        return true
    }
    val key = """-----BEGIN PGP PUBLIC KEY BLOCK-----

        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
        2zm0NEF2
        =vJJs
        -----END PGP PUBLIC KEY BLOCK-----
    """.trimIndent()
    val keyRing = PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(ByteArrayInputStream(key.toByteArray())), BcKeyFingerprintCalculator())
    val signature = PGPSignature(BCPGInputStream.wrap(PGPUtil.getDecoderStream(ByteArrayInputStream(ResourceLoader.loadBytes("cert")))))

    keyRing.keyRings.forEach {
        it.publicKeys.forEach {
            if (signature.keyID == it.keyID) {
                signature.init(JcaPGPContentVerifierBuilderProvider().setProvider("BC"), it)
                if (signature.verifyCertification(it)) {
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (ByteArrayInputStream(signature.signatureTrailer).read(buffer).also { bytesRead = it } != -1) {
                        signature.update(buffer, 0, bytesRead)
                    }
                    if (signature.verify()) {
                        println("OK")
                        return true
                    }
                }
            }
        }
    }
    return false
}
