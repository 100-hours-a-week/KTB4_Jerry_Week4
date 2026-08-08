package ktb.fullstack.talktalk.global.handler;

import ktb.fullstack.talktalk.global.exception.BusinessException;
import ktb.fullstack.talktalk.global.exception.ErrorCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Component
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage, Throwable ex) {

        BusinessException be = unwrapBusiness(ex);
        if (be != null) {
            return errorFrame(be.getErrorCode());
        }
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private BusinessException unwrapBusiness(Throwable ex) {

        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof BusinessException be) {
                return be;
            }
        }
        return null;
    }

    private Message<byte[]> errorFrame(ErrorCode code) {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(code.getMessage());
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
