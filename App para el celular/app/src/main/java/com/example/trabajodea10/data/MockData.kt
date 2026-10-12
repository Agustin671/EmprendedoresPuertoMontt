package com.example.trabajodea10.data

object MockData {

    val asociaciones = listOf(
        Asociacion("as1", "Manos de Valle", "Villa del Valle", 0xFF8E6B94),
        Asociacion("as2", "Red del Río", "Puerto Norte", 0xFF3E7D6E),
        Asociacion("as3", "Taller Costero", "Playa Hermosa", 0xFFC07A3E)
    )

    val emprendedoras = listOf(
        Emprendedora(
            id = "emp1",
            nombre = "Camila Rojas",
            email = "camila.rojas@trama.app",
            telefono = "+54 9 341 555 0141",
            asociacionId = "as1",
            estado = EstadoCuenta.ACTIVA,
            color = 0xFF8E6B94,
            seguidores = 148
        ),
        Emprendedora(
            id = "emp2",
            nombre = "Lucía Fernández",
            email = "lucia.fernandez@trama.app",
            telefono = "+54 9 341 555 0192",
            asociacionId = "as1",
            estado = EstadoCuenta.ACTIVA,
            color = 0xFF3E7D6E,
            seguidores = 96
        ),
        Emprendedora(
            id = "emp3",
            nombre = "Marina Sosa",
            email = "marina.sosa@trama.app",
            telefono = "+54 9 342 555 0117",
            asociacionId = "as2",
            estado = EstadoCuenta.ACTIVA,
            color = 0xFFC07A3E,
            seguidores = 210
        ),
        Emprendedora(
            id = "emp4",
            nombre = "Sofía Benítez",
            email = "sofia.benitez@trama.app",
            telefono = "+54 9 342 555 0173",
            asociacionId = "as2",
            estado = EstadoCuenta.INVITADA,
            color = 0xFF6B7FB3,
            seguidores = 34
        ),
        Emprendedora(
            id = "emp5",
            nombre = "Rocío Giménez",
            email = "rocio.gimenez@trama.app",
            telefono = "+54 9 340 555 0128",
            asociacionId = "as3",
            estado = EstadoCuenta.SUSPENDIDA,
            color = 0xFFB36B7F,
            seguidores = 61
        ),
        Emprendedora(
            id = "emp6",
            nombre = "Paul Quiroga",
            email = "paul.quiroga@alerce.app",
            telefono = "+56 9 8765 4321",
            asociacionId = null,
            estado = EstadoCuenta.ACTIVA,
            color = 0xFF7A6B3E,
            seguidores = 129
        ),
        Emprendedora(
            id = "emp7",
            nombre = "Paula Medina",
            email = "paula.medina@trama.app",
            telefono = "+54 9 343 555 0104",
            asociacionId = null,
            estado = EstadoCuenta.INVITADA,
            color = 0xFF3E7DA0,
            seguidores = 12
        ),
        Emprendedora(
            id = "emp8",
            nombre = "Daniela Vargas",
            email = "daniela.vargas@trama.app",
            telefono = "+54 9 342 555 0159",
            asociacionId = "as3",
            estado = EstadoCuenta.ACTIVA,
            color = 0xFF9B4A6B,
            seguidores = 187
        )
    )

    val ferias = listOf(
        Feria(
            id = "f1",
            nombre = "Feria de Emprendedoras del Valle",
            ubicacion = "Plaza San Martín, Villa del Valle",
            fechaInicio = "2026-10-07",
            fechaFin = "2026-10-07",
            cupos = 40,
            precioEntrada = 2000.0,
            estado = EstadoFeria.EN_CURSO,
            participaciones = listOf(
                Participacion("emp1", EstadoAsistencia.PRESENTE),
                Participacion("emp2", EstadoAsistencia.PRESENTE),
                Participacion("emp3", EstadoAsistencia.PENDIENTE),
                Participacion("emp4", EstadoAsistencia.PENDIENTE),
                Participacion("emp6", EstadoAsistencia.PRESENTE),
                Participacion("emp8", EstadoAsistencia.RECHAZADA)
            )
        ),
        Feria(
            id = "f2",
            nombre = "Artesanías del Río – Primavera",
            ubicacion = "Costanera del Río, Puerto Norte",
            fechaInicio = "2026-11-08",
            fechaFin = "2026-11-09",
            cupos = 30,
            precioEntrada = 1500.0,
            estado = EstadoFeria.PROXIMA,
            participaciones = listOf(
                Participacion("emp1", invitacion = EstadoInvitacion.CONFIRMADA),
                Participacion("emp2", invitacion = EstadoInvitacion.PENDIENTE),
                Participacion("emp3", invitacion = EstadoInvitacion.CONFIRMADA),
                Participacion("emp6", invitacion = EstadoInvitacion.PENDIENTE)
            )
        ),
        Feria(
            id = "f3",
            nombre = "Mercado de Diseño Independiente",
            ubicacion = "Centro Cultural Municipal, Centro",
            fechaInicio = "2026-12-05",
            fechaFin = "2026-12-06",
            cupos = 50,
            precioEntrada = 0.0,
            estado = EstadoFeria.PROXIMA,
            participaciones = listOf(
                Participacion("emp1", invitacion = EstadoInvitacion.PENDIENTE),
                Participacion("emp4", invitacion = EstadoInvitacion.PENDIENTE),
                Participacion("emp7", invitacion = EstadoInvitacion.PENDIENTE)
            )
        ),
        Feria(
            id = "f4",
            nombre = "Feria Navideña Artesanal",
            ubicacion = "Plaza Principal, Villa del Valle",
            fechaInicio = "2026-12-19",
            fechaFin = "2026-12-20",
            cupos = 60,
            precioEntrada = 2500.0,
            estado = EstadoFeria.BORRADOR,
            participaciones = emptyList()
        )
    )

    val productos = listOf(
        Producto(
            id = "p1",
            nombre = "Bufanda de lana merino",
            categoria = Categoria.TEXTIL,
            precio = 18500.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFF8E6B94,
            variantes = listOf(
                Variante("p1v1", "Verde / M", 3),
                Variante("p1v2", "Verde / L", 2),
                Variante("p1v3", "Burdeos / M", 0)
            )
        ),
        Producto(
            id = "p2",
            nombre = "Bolso tejido a mano",
            categoria = Categoria.TEXTIL,
            precio = 32000.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF3E7D6E,
            variantes = listOf(
                Variante("p2v1", "Natural / S", 4),
                Variante("p2v2", "Natural / M", 1)
            )
        ),
        Producto(
            id = "p3",
            nombre = "Cinturón de cuero",
            categoria = Categoria.CUERO,
            precio = 24500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF8A5A3B,
            variantes = listOf(
                Variante("p3v1", "Marrón / 85", 0),
                Variante("p3v2", "Marrón / 90", 2)
            )
        ),
        Producto(
            id = "p4",
            nombre = "Aros de plata martillada",
            categoria = Categoria.JOYERIA,
            precio = 12800.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFF6B7FB3,
            variantes = listOf(Variante("p4v1", "Único", 6))
        ),
        Producto(
            id = "p5",
            nombre = "Vela de soja aromática",
            categoria = Categoria.DECORACION,
            precio = 9900.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFFC07A3E,
            variantes = listOf(
                Variante("p5v1", "Lavanda", 8),
                Variante("p5v2", "Vainilla", 5)
            )
        ),
        Producto(
            id = "p6",
            nombre = "Jabón artesanal de romero",
            categoria = Categoria.COSMETICA,
            precio = 4500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF7FA66B,
            variantes = listOf(Variante("p6v1", "Único", 12))
        ),
        Producto(
            id = "p7",
            nombre = "Mantel de lino bordado",
            categoria = Categoria.TEXTIL,
            precio = 46000.0,
            publicado = false,
            destacado = false,
            colorFoto = 0xFFB36B7F,
            variantes = listOf(Variante("p7v1", "Único", 0))
        ),
        Producto(
            id = "p8",
            nombre = "Tabla de servir de guadua",
            categoria = Categoria.DECORACION,
            precio = 21000.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF9B7A3E,
            variantes = listOf(
                Variante("p8v1", "Grande", 2),
                Variante("p8v2", "Mediana", 0)
            )
        ),
        Producto(
            id = "p9",
            nombre = "Mermelada de mora",
            categoria = Categoria.GOURMET,
            precio = 6800.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF7B3E7A,
            variantes = listOf(Variante("p9v1", "380 g", 9))
        ),
        Producto(
            id = "p10",
            tiendaId = "emp1",
            nombre = "Pañuelo de seda pintado",
            categoria = Categoria.TEXTIL,
            precio = 15400.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFFA0546B,
            variantes = listOf(Variante("p10v1", "Único", 1))
        ),
        // Productos para Repostería Tradición Sureña
        Producto(
            id = "p_rep_1",
            tiendaId = "t_reposteria_surena",
            nombre = "Kuchen Tradicional de Murta y Frambuesa",
            categoria = Categoria.GOURMET,
            precio = 12000.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFFD69E2E,
            variantes = listOf(Variante("v_rep1", "Molde Familiar (8 porciones)", 4))
        ),
        Producto(
            id = "p_rep_2",
            tiendaId = "t_reposteria_surena",
            nombre = "Torta de Hojarasca con Manjar Casero",
            categoria = Categoria.GOURMET,
            precio = 18500.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFFC53030,
            variantes = listOf(Variante("v_rep2", "15 Personas", 2))
        ),
        Producto(
            id = "p_rep_3",
            tiendaId = "t_reposteria_surena",
            nombre = "Pack 6 Alfajores Artesanales con Nuez",
            categoria = Categoria.GOURMET,
            precio = 5500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF8A5A3B,
            variantes = listOf(Variante("v_rep3", "Caja 6 un.", 8))
        ),
        Producto(
            id = "p_rep_4",
            tiendaId = "t_reposteria_surena",
            nombre = "Galletas Horneadas de Avena y Miel de Ulmo",
            categoria = Categoria.GOURMET,
            precio = 3800.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFFC07A3E,
            variantes = listOf(Variante("v_rep4", "Bolsa 250g", 12))
        ),
        // Productos para Minimarket Doña Rosa
        Producto(
            id = "p_mini_1",
            tiendaId = "t_minimarket_rosa",
            nombre = "Pan Amasado Caliente (Salida 17:00 hrs)",
            categoria = Categoria.GOURMET,
            precio = 2500.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFFDD6B20,
            variantes = listOf(Variante("v_min1", "Bolsa 5 unidades", 20))
        ),
        Producto(
            id = "p_mini_2",
            tiendaId = "t_minimarket_rosa",
            nombre = "Canasta de Verduras Frescas de Alerce",
            categoria = Categoria.GOURMET,
            precio = 7000.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF2E7D5B,
            variantes = listOf(Variante("v_min2", "Surtido Semanal", 5))
        ),
        Producto(
            id = "p_mini_3",
            tiendaId = "t_minimarket_rosa",
            nombre = "Huevos de Campo Libres (Docena)",
            categoria = Categoria.GOURMET,
            precio = 4200.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFFD69E2E,
            variantes = listOf(Variante("v_min3", "Docena", 15))
        ),
        Producto(
            id = "p_mini_4",
            tiendaId = "t_minimarket_rosa",
            nombre = "Miel de Ulmo Pura de Bosque Nativo 1kg",
            categoria = Categoria.GOURMET,
            precio = 6500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFFC07A3E,
            variantes = listOf(Variante("v_min4", "Frasco 1kg", 6))
        ),
        // Productos para Tejidos Alerce Austral
        Producto(
            id = "p_tej_1",
            tiendaId = "t_tejidos_austral",
            nombre = "Chaleco de Lana Chilota a Palillo",
            categoria = Categoria.TEXTIL,
            precio = 38000.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFF5C315B,
            variantes = listOf(
                Variante("v_tej1", "Gris Natural · Talla M", 2),
                Variante("v_tej2", "Blanco Crudo · Talla L", 1)
            )
        ),
        Producto(
            id = "p_tej_2",
            tiendaId = "t_tejidos_austral",
            nombre = "Gorro y Cuello de Lana Pura de Oveja",
            categoria = Categoria.TEXTIL,
            precio = 14000.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF2E7D5B,
            variantes = listOf(Variante("v_tej3", "Talla Única", 6))
        ),
        Producto(
            id = "p_tej_3",
            tiendaId = "t_tejidos_austral",
            nombre = "Piecera Rústica en Telar Tradicional",
            categoria = Categoria.DECORACION,
            precio = 45000.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFFC07A3E,
            variantes = listOf(Variante("v_tej4", "2 Plazas", 2))
        ),
        // Servicios / Productos para Barbería & Estilo Alerce
        Producto(
            id = "p_bar_1",
            tiendaId = "t_barberia_alerce",
            nombre = "Corte de Cabello Clásico / Degrade Moderno",
            categoria = Categoria.COSMETICA,
            precio = 8000.0,
            publicado = true,
            destacado = true,
            colorFoto = 0xFF2D3748,
            variantes = listOf(Variante("v_bar1", "Sesión 40 min", 10))
        ),
        Producto(
            id = "p_bar_2",
            tiendaId = "t_barberia_alerce",
            nombre = "Perfilado y Ritual de Barba con Toalla Caliente",
            categoria = Categoria.COSMETICA,
            precio = 6500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF319795,
            variantes = listOf(Variante("v_bar2", "Sesión 30 min", 8))
        ),
        Producto(
            id = "p_bar_3",
            tiendaId = "t_barberia_alerce",
            nombre = "Cera y Pomada Modeladora Capilar",
            categoria = Categoria.COSMETICA,
            precio = 7500.0,
            publicado = true,
            destacado = false,
            colorFoto = 0xFF4A5568,
            variantes = listOf(Variante("v_bar3", "Pote 100g", 12))
        )
    )

    val ventas = listOf(
        Venta(
            id = "v1",
            fecha = "2026-10-07 11:24",
            items = listOf(
                ItemVenta("p1", "p1v1", "Bufanda de lana merino", "Verde / M", 18500.0, 1),
                ItemVenta("p6", "p6v1", "Jabón artesanal de romero", "Único", 4500.0, 2)
            ),
            medioPago = MedioPago.EFECTIVO,
            feriaId = "f1",
            total = 27500.0
        ),
        Venta(
            id = "v2",
            fecha = "2026-10-07 12:03",
            items = listOf(
                ItemVenta("p4", "p4v1", "Aros de plata martillada", "Único", 12800.0, 1)
            ),
            medioPago = MedioPago.QR,
            feriaId = "f1",
            total = 12800.0
        ),
        Venta(
            id = "v3",
            fecha = "2026-10-06 17:40",
            items = listOf(
                ItemVenta("p2", "p2v1", "Bolso tejido a mano", "Natural / S", 32000.0, 1)
            ),
            medioPago = MedioPago.TRANSFERENCIA,
            feriaId = null,
            total = 32000.0
        ),
        Venta(
            id = "v4",
            fecha = "2026-10-04 10:15",
            items = listOf(
                ItemVenta("p5", "p5v1", "Vela de soja aromática", "Lavanda", 9900.0, 3)
            ),
            medioPago = MedioPago.DEBITO,
            feriaId = "f1",
            total = 29700.0
        ),
        Venta(
            id = "v5",
            fecha = "2026-10-02 16:50",
            items = listOf(
                ItemVenta("p9", "p9v1", "Mermelada de mora", "380 g", 6800.0, 2),
                ItemVenta("p10", "p10v1", "Pañuelo de seda pintado", "Único", 15400.0, 1)
            ),
            medioPago = MedioPago.EFECTIVO,
            feriaId = null,
            total = 29000.0
        ),
        Venta(
            id = "v6",
            fecha = "2026-10-01 13:08",
            items = listOf(
                ItemVenta("p3", "p3v2", "Cinturón de cuero", "Marrón / 90", 24500.0, 1)
            ),
            medioPago = MedioPago.TRANSFERENCIA,
            feriaId = null,
            total = 24500.0
        )
    )

    val tienda = Tienda(
        nombre = "Tejidos del Sur",
        bio = "Piezas textiles y cuero artesanal, elaboradas a mano en Villa del Valle. Envíos a todo el país.",
        whatsapp = "+54 9 341 555 0141",
        instagram = "@tejidosdelsur",
        colorPortada = 0xFF5C315B,
        colorLogo = 0xFF3E7D6E
    )
}
