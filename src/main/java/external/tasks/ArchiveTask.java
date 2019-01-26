package external.tasks;

import org.camunda.bpm.client.ExternalTaskClient;

public class ArchiveTask {

    public static void main(String... args) throws InterruptedException {
        // bootstrap the client
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/rest")
                .build();

        // subscribe to the topic
        client.subscribe("Archive")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {

                    externalTaskService.complete(externalTask);

                    System.out.println("The External Archive Task " + externalTask.getId() + " has been completed!");

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("The External Archive Task was interrupted.");
                    }

                }).open();
    }
}
