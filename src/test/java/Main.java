import com.codingame.gameengine.runner.SoloGameRunner;
import com.codingame.gameengine.runner.simulate.GameResult;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--simulate".equals(args[0])) {
            runBatchSimulation();
            return;
        }
        if (!ensureSystemClasspath(args)) {
            return;
        }

        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setAgent(Solution.class);
        gameRunner.setTestCase("test1.json");
        gameRunner.start(8888);
    }

    private static boolean ensureSystemClasspath(String[] args) {
        if (ClassLoader.getSystemClassLoader().getResource("view") != null) {
            return true;
        }
        ClassLoader cl = Main.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                StringBuilder cp = new StringBuilder();
                for (URL url : urls) {
                    if (cp.length() > 0) cp.append(File.pathSeparator);
                    cp.append(new File(url.toURI()).getAbsolutePath());
                }
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                List<String> cmd = new ArrayList<>();
                cmd.add(javaBin);
                cmd.add("-cp");
                cmd.add(cp.toString());
                cmd.add(Main.class.getName());
                cmd.addAll(Arrays.asList(args));
                Process p = new ProcessBuilder(cmd).inheritIO().start();
                Runtime.getRuntime().addShutdownHook(new Thread(p::destroy));
                p.waitFor();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static void runBatchSimulation() {
        int totalGold = 0;
        for (int i = 1; i <= 3; i++) {
            SoloGameRunner runner = new SoloGameRunner();
            runner.setAgent(Solution.class);
            runner.setTestCase("test" + i + ".json");
            GameResult result = runner.simulate();
            String meta = result.metadata == null ? "{}" : result.metadata;
            System.out.printf("[Test %d] Metadata = %s (Frames=%d)%n", i, meta.trim(), result.views.size());
        }
        System.out.printf("==> Total Optimization Score across Validators: %d Gold%n", totalGold);
    }
}
