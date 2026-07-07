package com.institucion.sigea;

import com.institucion.sigea.config.properties.JwtProperties;
import com.institucion.sigea.security.jwt.JwtUtil;

public class GenerarTokenPrueba {

    public static void main(String[] args) {
        JwtProperties jwtProperties = new JwtProperties(
                "6UdiLAwSKX2VNppWkj2stJsMRxirnMT5yPjLZ+28QVM=", 86400000);
        JwtUtil jwtUtil = new JwtUtil(jwtProperties);

        String token = jwtUtil.generateToken(1L, "prueba", "SUPERUSUARIO", true);
        System.out.println("TOKEN DE PRUEBA: " + token);
    }
}