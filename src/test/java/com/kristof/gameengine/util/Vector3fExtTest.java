package com.kristof.gameengine.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector3fExtTest {
    @org.junit.jupiter.api.Test
    void getReverse() {
        final Vector3fExt vec = new Vector3fExt(1f, 2f, 3f);
        final Vector3fExt reverseVec = vec.getReverse();
        assertEquals(vec.getX(), -reverseVec.getX());
        assertEquals(vec.getY(), -reverseVec.getY());
        assertEquals(vec.getZ(), -reverseVec.getZ());
    }

    @Test
    void reverseDirection() {
        final Vector3fExt tmpVec = new Vector3fExt(1f, 2f, 3f);
        final Vector3fExt origVec = tmpVec.getCopy();
        tmpVec.reverseDirection();
        assertEquals(origVec.getX(), -tmpVec.getX());
        assertEquals(origVec.getY(), -tmpVec.getY());
        assertEquals(origVec.getZ(), -tmpVec.getZ());
    }
}