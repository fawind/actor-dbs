package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.InsertRowMsg;
import messages.QueryErrorMsg;
import messages.QuerySuccessMsg;
import messages.SelectAllMsg;
import messages.SelectWhereMsg;
import messages.TransactionMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Master extends AbstractDBActor {

    public static final String ACTOR_NAME = "master";

    private Map<String, ActorRef> tables;

    private Master() {
        this.tables = new HashMap<>();
    }

    static Props props() {
        return Props.create(Master.class, Master::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateTableMsg.class, this::handleCreateTable)
                .match(InsertMsg.class, this::handleInsert)
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleInsert(InsertMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(new InsertRowMsg(msg.getRow(), msg.getTransaction()), getSelf()));
    }

    private void handleCreateTable(CreateTableMsg msg) {
        String tableName = msg.getTableName();
        if (tables.containsKey(tableName)) {
            msg.getRequester().tell(new QueryErrorMsg("Table '" + tableName + "' already exists.", msg.getTransaction()),
                    getSelf());
            return;
        }

        String actorName = "table-actor_" + tableName;
        ActorRef table = getContext().actorOf(Table.props(msg.getTableName(), msg.getLayout(), getSelf()), actorName);

        log.info("Created actor: " + actorName);

        tables.put(msg.getTableName(), table);
        msg.getRequester().tell(new QuerySuccessMsg(msg.getTransaction()), getSelf());
    }

    private void handleSelectAll(SelectAllMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(msg, msg.getRequester()));
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(msg, msg.getRequester()));
    }


    private Optional<ActorRef> assertTableExists(String tableName, TransactionMsg msg) {
        ActorRef table = tables.get(tableName);
        if (table == null) {
            msg.getRequester().tell(new QueryErrorMsg("Table '" + tableName + "' does not exist.", msg.getTransaction()), getSelf());
            return Optional.empty();
        } else {
            return Optional.of(table);
        }
    }
}
