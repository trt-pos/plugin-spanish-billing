package org.lebastudios.theroundtable.pluginspanishbilling.ordering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberTest
{
    @Test
    void next()
    {
        assertEquals("0000000001", Number.next("0000000000"));
        assertEquals("0000000010", Number.next("0000000009"));
        assertEquals("0000000011", Number.next("0000000010"));
        assertEquals("0000000020", Number.next("0000000019"));
        assertEquals("0000000021", Number.next("0000000020"));
        assertEquals("0000000030", Number.next("0000000029"));
        assertEquals("0000000031", Number.next("0000000030"));
        assertEquals("0000000040", Number.next("0000000039"));
        assertEquals("0000000041", Number.next("0000000040"));
        assertEquals("0000000050", Number.next("0000000049"));
        assertEquals("0000000051", Number.next("0000000050"));
        assertEquals("0000000060", Number.next("0000000059"));
        assertEquals("0000000061", Number.next("0000000060"));
        assertEquals("0000000070", Number.next("0000000069"));
        assertEquals("0000000071", Number.next("0000000070"));
        assertEquals("0000000080", Number.next("0000000079"));
        assertEquals("0000000081", Number.next("0000000080"));
        assertEquals("0000000090", Number.next("0000000089"));
        assertEquals("0000000091", Number.next("0000000090"));
        assertEquals("0000000100", Number.next("0000000099"));
        assertEquals("0000000101", Number.next("0000000100"));
        assertEquals("0000000110", Number.next("0000000109"));
        assertEquals("0000000111", Number.next("0000000110"));
        assertEquals("0000000120", Number.next("0000000119"));
        assertEquals("0000000121", Number.next("0000000120"));
        assertEquals("0000000130", Number.next("0000000129"));
        assertEquals("0000000131", Number.next("0000000130"));
        assertEquals("0000000140", Number.next("0000000139"));
        assertEquals("0000000141", Number.next("0000000140"));
        assertEquals("0000000150", Number.next("0000000149"));
        assertEquals("0000000151", Number.next("0000000150"));
        
        assertEquals("11", Number.next("10"));
        assertEquals("10", Number.next("9"));
        assertEquals("10", Number.next("09"));
        assertEquals("099", Number.next("098"));
        
        assertEquals("0000999999", Number.next("0000999998"));
        assertEquals("0001000000", Number.next("0000999999"));
    }
}