package ru.mipt.cybersecurity.jce.interfaces;

import java.security.PublicKey;

import ru.mipt.cybersecurity.math.ec.ECPoint;

/**
 * interface for elliptic curve public keys.
 */
public interface ECPublicKey
    extends ECKey, PublicKey
{
    /**
     * return the public point Q
     */
    public ECPoint getQ();
}
