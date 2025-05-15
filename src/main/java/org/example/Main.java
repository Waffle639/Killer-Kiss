package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String nomPersonesJson = "persones.json";
        String nomPartidesJson = "partides.json";
        ArrayList<Persona> persones = cargarPersonasDelJson(nomPersonesJson);
        ArrayList<KillerKiss> partides = cargarPartidesDelJson(nomPartidesJson);
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
                    afegirPersona(persones);
                    break;
                case 2:
                    System.out.println("--------------Eliminar persona------------------");
                    for (int i = 0; i < persones.size(); i++) {
                        System.out.println(i + 1 + " - " + persones.get(i).getNom());
                    }
                    System.out.print("Introduce el número de la persona a eliminar: ");
                    int numPersona = demenar_numero(1, persones.size()) - 1;
                    if (numPersona >= 0 && numPersona < persones.size()) {
                        System.out.println("Persona eliminada: " + persones.get(numPersona).getNom());
                        persones.remove(numPersona);
                    } else {
                        System.out.println("Número de persona no válido.");
                    }

                case 3:
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
                        String resposta2;
                        if (resposta.equalsIgnoreCase("si")) {
                            do {
                                System.out.println("¿Quieres eliminar o añadir participantes? (eliminar/añadir/continuar)");
                                resposta2 = sc.nextLine();
                                if (resposta2.equalsIgnoreCase("eliminar")) {
                                    for (int i = 0; i < persones.size(); i++) {
                                        System.out.println(i + 1 + " - " + persones.get(i).getNom());
                                    }
                                    System.out.println("Introduce el número de la persona a eliminar: ");
                                    int numEliminar = demenar_numero(1, persones.size()) - 1;
                                    if (numEliminar >= 0 && numEliminar < persones.size()) {
                                        System.out.println("Persona eliminada: " + persones.get(numEliminar).getNom());
                                        partida.eliminarPersona(persones.get(numEliminar));
                                    } else {
                                        System.out.println("Número de persona no válido.");
                                    }
                                } else if (resposta2.equalsIgnoreCase("añadir")) {
                                    partida.afegirPersona(afegirPersona(persones));
                                }
                            } while (!resposta2.equalsIgnoreCase("continuar"));
                        }
                        partida.sortPersonas();
                        for (int i = 0; i < partida.getPersonas().size(); i++) {
                            String missatge = "Hola " + partida.getPersonas().get(i).getNom() + ",\n\n" +
                                    "¡Bienvenido a la partida de Killer Kiss!\n" +
                                    "Tu víctima es: " + partida.getPersonas().get((i + 1) % partida.getPersonas().size()).getNom() + ".\n";
                            enviarCorreu(partida.getPersonas().get(i).getMail(), missatge, "Partida Killer Kiss");
                        }
                        partides.add(partida);
                    }
                    break;
                case 5:
                    System.out.println("--------------Terminar partida------------------");
                    ArrayList<Integer> indicesActivas = new ArrayList<>();
                    for (int i = 0; i < partides.size(); i++) {
                        if (partides.get(i).getEstat()) {
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
                    int seleccion = demenar_numero(1, indicesActivas.size()) - 1;
                    int numPartida = indicesActivas.get(seleccion);

                    System.out.println("Introdueix el numero del guanyador: ");
                    KillerKiss partidaSeleccionada = partides.get(numPartida);
                    for (int i = 0; i < partidaSeleccionada.getPersonas().size(); i++) {
                        System.out.println((i + 1) + " - " + partidaSeleccionada.getPersonas().get(i).getNom());
                    }

                    int numGuanyador = demenar_numero(1, partidaSeleccionada.getPersonas().size()) - 1;
                    if (numGuanyador >= 0 && numGuanyador < partidaSeleccionada.getPersonas().size()) {
                        System.out.println("Ganador: " + partidaSeleccionada.getPersonas().get(numGuanyador).getNom());
                        Persona guanyador = partidaSeleccionada.getPersonas().get(numGuanyador);
                        for (Persona p : persones) {
                            if (p.equals(guanyador)) {
                                p.sumarVictoria();
                                break;
                            }
                        }
                        partidaSeleccionada.acavarPartida(guanyador);
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
                                        partides.stream().filter(KillerKiss::getEstat).count());
                                System.out.println("Partidas finalizadas: " +
                                        partides.stream().filter(p -> !p.getEstat()).count());
                                break;

                            case 2:
                                System.out.println("--- Ranking de Jugadores ---");
                                ArrayList<Persona> ranking = new ArrayList<>(persones);
                                ranking.sort((p1, p2) -> Integer.compare(p2.getVictories(), p1.getVictories()));

                                System.out.println("Pos.  Nombre    Victorias     Sección");
                                System.out.println("------------------------------------------");
                                for (int i = 0; i < ranking.size(); i++) {
                                    Persona p = ranking.get(i);
                                    System.out.println((i+1) + ".   " + p.getNom() +
                                            " - Victorias: " + p.getVictories() +
                                            " - Sección: " + p.getseccio());
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

        guardarPersonasEnFitxer(persones, nomPersonesJson);
        guardarPartidesEnFitxer(partides, nomPartidesJson);
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

    private static void guardarPersonasEnFitxer(ArrayList<Persona> personas, String nomFitxerJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(nomFitxerJson)) {
            gson.toJson(personas, writer);
            System.out.println("Datos guardados correctamente en el archivo: " + nomFitxerJson);
        } catch (IOException e) {
            System.err.println("Error al guardar en el archivo JSON: " + e.getMessage());
        }
    }

    private static ArrayList<Persona> cargarPersonasDelJson(String nomFitxerJson) {
        Gson gson = new Gson();
        ArrayList<Persona> personas = new ArrayList<>();

        try (Reader reader = new FileReader(nomFitxerJson)) {
            Type tipuscanso = new TypeToken<List<Persona>>() {
            }.getType();

            personas = gson.fromJson(reader, tipuscanso);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }

        return personas;
    }

    private static void guardarPartidesEnFitxer(ArrayList<KillerKiss> killerKisses, String nomFitxerJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer writer = new FileWriter(nomFitxerJson)) {
            gson.toJson(killerKisses, writer);
            System.out.println("Datos guardados correctamente en el archivo: " + nomFitxerJson);
        } catch (IOException e) {
            System.err.println("Error al guardar en el archivo JSON: " + e.getMessage());
        }
    }

    private static ArrayList<KillerKiss> cargarPartidesDelJson(String nomFitxerJson) {
        Gson gson = new Gson();
        ArrayList<KillerKiss> killerKisses = new ArrayList<>();

        try (Reader reader = new FileReader(nomFitxerJson)) {
            Type tipuscanso = new TypeToken<List<KillerKiss>>() {
            }.getType();

            killerKisses = gson.fromJson(reader, tipuscanso);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }

        return killerKisses;
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
        System.out.print("Introduce la sección de la persona: ");
        String seccio = sc.nextLine();
        System.out.print("Introduce el correo de la persona: ");
        String mail = sc.nextLine();
        Persona persona = new Persona(nom, seccio, mail);
        persones.add(persona);
        System.out.println("Persona añadida correctamente.");
        return persona;
    }
}
