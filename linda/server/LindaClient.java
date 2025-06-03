package linda.server;

import linda.*;
import java.rmi.*;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


    public class LindaClient implements Linda {
        private RemoteLinda remoteLinda;
        private final List<Tuple> lastReadTemplates = new ArrayList<>();


        public LindaClient(String serverURI) {
        try {
            this.remoteLinda = (RemoteLinda) Naming.lookup(serverURI);
            System.out.println("CLIENT : connecté au serveur");

            // 👇 Partie ajoutée : s’abonner au ERASEALL
            setupEraseAllCallback();

        } catch (Exception e) {
            System.err.println("Échec de connexion au serveur: " + e.getMessage());
        }
    }


    @Override
    public void write(Tuple t) {
        try {
            remoteLinda.write(t);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur write RMI", e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return remoteLinda.take(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur take RMI", e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return remoteLinda.read(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur read RMI", e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return remoteLinda.tryTake(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur tryTake RMI", e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return remoteLinda.tryRead(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur tryRead RMI", e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return remoteLinda.takeAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur takeAll RMI", e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return remoteLinda.readAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur readAll RMI", e);
        }
    }

    /** Enregistre un callback côté client pour écouter les ERASEALL */
    private void setupEraseAllCallback() {
        try {
            // Création du motif ERASEALL
            Tuple eraseMotif = new Tuple("Whiteboard",
                Enum.valueOf(
                    (Class<Enum>) (Class<?>) Class.forName("linda.whiteboard.WhiteboardModel$Command"),
                    "ERASEALL"
                ));
            System.out.println("CLIENT : enregistrement du callback ERASEALL pour " + eraseMotif);

            // On crée une référence finale pour la réutiliser dans le callback
            final RemoteCallback[] eraseCbHolder = new RemoteCallback[1];

            // Création du stub de callback RMI
            eraseCbHolder[0] = new AsynchronousCallbackImpl(new Callback() {
                public void call(Tuple t) {
                    System.out.println("CLIENT : reçu ERASEALL → resynchronisation");

                    for (Tuple template : lastReadTemplates) {
                        try {
                            Collection<Tuple> rest = readAll(template);
                            System.out.println("CLIENT : tuples encore présents après ERASE :");
                            for (Tuple rt : rest) {
                                System.out.println("  ↪ " + rt);
                            }
                        } catch (Exception e) {
                            System.err.println("CLIENT : erreur relecture post-ERASE");
                            e.printStackTrace();
                        }
                    }

                    // Réenregistrement du callback
                    try {
                        remoteLinda.eventRegister(eventMode.READ, eventTiming.FUTURE, eraseMotif, eraseCbHolder[0]);
                    } catch (RemoteException e) {
                        System.err.println("CLIENT : erreur re-registration ERASEALL");
                        e.printStackTrace();
                    }
                }
            });

            // Enregistrement initial
            remoteLinda.eventRegister(eventMode.READ, eventTiming.FUTURE, eraseMotif, eraseCbHolder[0]);

        } catch (Exception e) {
            System.err.println("CLIENT : erreur lors du setup ERASEALL");
            e.printStackTrace();
        }
    }


    @Override
    public void eventRegister(eventMode mode, eventTiming timing,
                            Tuple template, Callback clientCallback) {
        try {
            RemoteCallback cbStub = new AsynchronousCallbackImpl(clientCallback);
            System.out.println("CLIENT : enregistrement d’un callback RMI sur " + template);
            remoteLinda.eventRegister(mode, timing, template, cbStub);

            // Sauvegarde du template, uniquement pour motifShape
            if (template.toString().contains("Whiteboard") &&
                template.toString().contains("DRAW")) {
                lastReadTemplates.add(template); // garde trace du motif DRAW
            }

        } catch (RemoteException e) {
            throw new RuntimeException("Erreur eventRegister RMI", e);
        }
    }


    @Override
    public void debug(String prefix) {
        try {
            remoteLinda.debug(prefix);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur debug RMI", e);
        }
    }

    public void clean_Tspace() {
        try {
            remoteLinda.clean_Tspace();
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur clean_Tspace RMI", e);
        }
    }
}


