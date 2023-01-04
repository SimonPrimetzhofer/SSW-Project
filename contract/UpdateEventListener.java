package contract;

import common.UpdateEvent;

public interface UpdateEventListener {
    void update(UpdateEvent e);
}
