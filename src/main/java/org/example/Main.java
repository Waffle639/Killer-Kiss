package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.example.repository.KillerKissRepository;
import org.example.repository.PersonaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@SpringBootApplication
@org.springframework.context.annotation.PropertySource("classpath:mail.config")
public class Main {
    
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /*
     * MENÚ DE CONSOLA - COMENTADO PARA USAR EL FRONTEND WEB
     * Si quieres usar el menú de consola, descomenta este método
     * Si quieres usar el frontend web, déjalo comentado
     */
    /*
    @Bean
    public CommandLineRunner run(PersonaRepository personaRepository, KillerKissRepository partidaRepository) {
        return args -> {
            Scanner sc = new Scanner(System.in);
            ArrayList<Persona> persones = new ArrayList<>(personaRepository.findAll());
            ArrayList<KillerKiss> partides = new ArrayList<>(partidaRepository.findAll());
        int opcio = -1;
        do {
            System.out.println("--------------Killer Kiss------------------");
            System.out.println("1 - Añadir persona");
            System.out.println("2 - Eliminar persona");
            System.out.println("3 - Listar personas");
            System.out.println("4 - Comenzar partida");
            System.out.println("5 - Terminar partida");
            System.out.println("6 - Estadísticas");
            System.out.println("0 - Salir y guardar datos");

            System.out.print("Introduce una opción: ");
            opcio = demenar_numero(0, 6);

            switch (opcio) {
                case 1:
                    System.out.println("--------------Añadir persona------------------");
                    Persona nuevaPersona = afegirPersona(persones);
                    personaRepository.save(nuevaPersona); // Guardar en BD
                    break;
                case 2:
                    if (persones.isEmpty()) {
                        System.out.println("No hay personas registradas.");
                        break;
                    }
                    System.out.println("--------------Eliminar persona------------------");
                    for (int i = 0; i < persones.size(); i++) {
                        System.out.println((i + 1) + " - " + persones.get(i).getNom());
                    }
                    System.out.println("0 - Salir sin eliminar");
                    System.out.print("Introduce el número de la persona a eliminar: ");
                    int numPersona = demenar_numero(0, persones.size());
                    if (numPersona == 0) {
                        System.out.println("Operación cancelada.");
                        break;
                    }
                    numPersona--; // Ajustar índice
                    if (numPersona >= 0 && numPersona < persones.size()) {
                        Persona personaAEliminar = persones.get(numPersona);
                        System.out.println("Persona eliminada: " + personaAEliminar.getNom());
                        personaRepository.delete(personaAEliminar); // Eliminar de BD
                        persones.remove(numPersona);
                    } else {
                        System.out.println("Número de persona no válido.");
                    }
                    break;

                case 3:
                    if (persones.isEmpty()) {
                        System.out.println("No hay personas registradas.");
                        break;
                    }
                    System.out.println("--------------Listar personas------------------");
                    for (int i = 0; i < persones.size(); i++) {
                        System.out.println(i + 1 + " - " + persones.get(i).getNom());
                    }
                    break;
                case 4:
                    System.out.println("--------------Comenzar partida------------------");
                    if (persones.size() < 2) {
                        System.out.println("No hay suficientes personas para comenzar una partida.");
                    } else {
                        System.out.println("Dime el nombre de la partida:");
                        String nomPartida = sc.nextLine();
                        KillerKiss partida = new KillerKiss(nomPartida, persones);
                        System.out.println("Participantes:");
                        for (int i = 0; i < persones.size(); i++) {
                            System.out.println(i + 1 + " - " + persones.get(i).getNom());
                        }
                        System.out.println("¿Quieres editar los participantes? (si/no)");
                        String resposta = sc.nextLine();
                        String resposta2 = "";
                        if (resposta.equalsIgnoreCase("si")) {
                            do {
                                System.out.println("¿Quieres eliminar o añadir participantes? (eliminar/añadir/continuar/salir)");
                                resposta2 = sc.nextLine();
                                if (resposta2.equalsIgnoreCase("salir")) {
                                    System.out.println("Partida cancelada.");
                                    break;
                                } else if (resposta2.equalsIgnoreCase("eliminar")) {
                                    for (int i = 0; i < persones.size(); i++) {
                                        System.out.println((i + 1) + " - " + persones.get(i).getNom());
                                    }
                                    System.out.println("0 - Volver sin eliminar");
                                    System.out.println("Introduce el número de la persona a eliminar: ");
                                    int numEliminar = demenar_numero(0, persones.size());
                                    if (numEliminar == 0) {
                                        System.out.println("Operación cancelada.");
                                    } else {
                                        numEliminar--; // Ajustar índice
                                        if (numEliminar >= 0 && numEliminar < persones.size()) {
                                            System.out.println("Persona eliminada: " + persones.get(numEliminar).getNom());
                                            partida.eliminarPersona(persones.get(numEliminar));
                                        } else {
                                            System.out.println("Número de persona no válido.");
                                        }
                                    }
                                } else if (resposta2.equalsIgnoreCase("añadir")) {
                                    partida.afegirPersona(afegirPersona(persones));
                                }
                            } while (!resposta2.equalsIgnoreCase("continuar") && !resposta2.equalsIgnoreCase("salir"));
                        }
                        
                        // Si se canceló la partida, no continuar
                        if (resposta.equalsIgnoreCase("si") && resposta2.equalsIgnoreCase("salir")) {
                            break;
                        }
                        
                        partida.sortPersonas();
                        partida.iniciarPartida(); // Iniciar partida
                        partidaRepository.save(partida); // Guardar en BD
                        partides.add(partida);
                        for (int i = 0; i < partida.getPersonas().size(); i++) {
                            String missatge = "Hola " + partida.getPersonas().get(i).getNom() + ",\n\n" +
                                    "¡Bienvenido a la partida de Killer Kiss!\n" +
                                    "Tu víctima es: " + partida.getPersonas().get((i + 1) % partida.getPersonas().size()).getNom() + ".\n";
                            enviarCorreu(partida.getPersonas().get(i).getMail(), missatge, "Partida Killer Kiss");
                        }
                    }
                    break;
                case 5:
                    System.out.println("--------------Terminar partida------------------");
                    ArrayList<Integer> indicesActivas = new ArrayList<>();
                    for (int i = 0; i < partides.size(); i++) {
                        if (partides.get(i).isEstat()) {
                            indicesActivas.add(i);
                        }
                    }
                    if (indicesActivas.isEmpty()) {
                        System.out.println("No hay partidas activas en curso.");
                        break;
                    }
                    System.out.println("Partidas activas:");
                    for (int i = 0; i < indicesActivas.size(); i++) {
                        int idxReal = indicesActivas.get(i);
                        System.out.println((i + 1) + " - " + partides.get(idxReal).getNom());
                    }
                    System.out.println("0 - Salir sin terminar partida");
                    System.out.print("Selecciona una partida: ");
                    int seleccion = demenar_numero(0, indicesActivas.size());
                    if (seleccion == 0) {
                        System.out.println("Operación cancelada.");
                        break;
                    }
                    seleccion--; // Ajustar índice
                    int numPartida = indicesActivas.get(seleccion);

                    System.out.println("Introdueix el numero del guanyador: ");
                    KillerKiss partidaSeleccionada = partides.get(numPartida);
                    for (int i = 0; i < partidaSeleccionada.getPersonas().size(); i++) {
                        System.out.println((i + 1) + " - " + partidaSeleccionada.getPersonas().get(i).getNom());
                    }
                    System.out.println("0 - Cancelar");

                    int numGuanyador = demenar_numero(0, partidaSeleccionada.getPersonas().size());
                    if (numGuanyador == 0) {
                        System.out.println("Operación cancelada.");
                        break;
                    }
                    numGuanyador--; // Ajustar índice
                    if (numGuanyador >= 0 && numGuanyador < partidaSeleccionada.getPersonas().size()) {
                        System.out.println("Ganador: " + partidaSeleccionada.getPersonas().get(numGuanyador).getNom());
                        Persona guanyador = partidaSeleccionada.getPersonas().get(numGuanyador);
                        for (Persona p : persones) {
                            if (p.getId().equals(guanyador.getId())) {
                                p.sumarVictoria();
                                personaRepository.save(p); // Guardar victoria en BD
                                break;
                            }
                        }
                        partidaSeleccionada.acavarPartida(guanyador);
                        partidaRepository.save(partidaSeleccionada); // Guardar partida finalizada en BD
                    } else {
                        System.out.println("Numero de ganador no valido");
                    }
                    break;
                case 6:
                    boolean tornarMenuPrincipal = false;
                    do {
                        System.out.println("--------------ESTADÍSTICAS------------------");
                        System.out.println("1. Mostrar total de partidas");
                        System.out.println("2. Mostrar ranking de jugadores");
                        System.out.println("0. Volver al menú principal");
                        System.out.print("Selecciona una opción: ");

                        int opcioEstadistiques = demenar_numero(0, 2);

                        switch (opcioEstadistiques) {
                            case 1:
                                System.out.println("--- TOTAL DE PARTIDAS ---");
                                System.out.println("Partidas totales: " + partides.size());
                                System.out.println("Partidas activas: " +
                                        partides.stream().filter(KillerKiss::isEstat).count());
                                System.out.println("Partidas finalizadas: " +
                                        partides.stream().filter(p -> !p.isEstat()).count());
                                break;

                            case 2:
                                System.out.println("--- Ranking de Jugadores ---");
                                List<Persona> ranking = personaRepository.findAllOrderByVictoriesDesc();
                            
                                System.out.println("Pos.  Nombre    Victorias ");
                                System.out.println("-----------------------------");
                                for (int i = 0; i < ranking.size(); i++) {
                                    Persona p = ranking.get(i);
                                    System.out.println((i+1) + ".   " + p.getNom() +
                                            " - Victorias: " + p.getVictories());
                                }
                                break;

                            case 0:
                                tornarMenuPrincipal = true;
                                break;
                        }
                    } while (!tornarMenuPrincipal);
                    break;
            }

        } while (opcio != 0);

        System.out.println("¡Hasta pronto! Los datos se han guardado automáticamente en la base de datos.");
        };
    }

    private static void enviarCorreu(String destinatari, String missatge, String assumpte) {
        String remitente = "";
        String contrasena = "";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, contrasena);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatari));
            message.setSubject(assumpte);
            message.setText(missatge);

            Transport.send(message);
            System.out.println("Correo enviado a " + destinatari + " correctamente");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static int demenar_numero(int min, int max) {
        Scanner sc = new Scanner(System.in);
        int numero = -1;
        do {
            System.out.print("Introduce un número entre " + min + " y " + max + ": ");
            try {
                numero = sc.nextInt();
                if (numero < min || numero > max) {
                    System.out.println("Número fuera de rango. Intenta nuevamente.");
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida. Intenta nuevamente.");
                sc.nextLine();
            }
        } while (numero < min || numero > max);
        return numero;
    }

    public static Persona afegirPersona(ArrayList<Persona> persones) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce el nombre de la persona: ");
        String nom = sc.nextLine();
        System.out.print("Introduce el correo de la persona: ");
        String mail = sc.nextLine();
        Persona persona = new Persona(nom, mail);
        persones.add(persona);
        System.out.println("Persona añadida correctamente.");
        return persona;
    }
    */
}
