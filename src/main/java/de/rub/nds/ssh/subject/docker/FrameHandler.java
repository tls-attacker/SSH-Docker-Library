/**
 * SSH-Attacker - A Modular Penetration Testing Framework for SSH
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.ssh.subject.docker;

import java.util.Collection;
import java.util.LinkedList;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;

public class FrameHandler extends ResultCallbackTemplate<FrameHandler, Frame> {
    private static String[] EMPTY_STR_ARR = new String[] {};

    private Collection<Frame> frames;

    public FrameHandler() {
        frames = new LinkedList<>();
    }

    @Override
    public void onNext(Frame object) {
        synchronized (frames) {
            frames.add(object);
        }
    }

    public String[] getLines() {
        Collection<String> ret = new LinkedList<>();
        boolean pending_newline = false;
        StringBuilder current_line = new StringBuilder();
        for (Frame frame : frames) {
            for (byte b : frame.getPayload()) {
                if (b == '\r') {
                    pending_newline = true;
                } else if (b == '\n' || pending_newline) {
                    // handle newline
                    pending_newline = false;
                    ret.add(current_line.toString());
                    current_line = new StringBuilder();
                    if (b != '\n') {
                        current_line.append((char) b);
                    }
                } else {
                    current_line.append((char) b);
                }
            }
        }
        if (current_line.length() > 0) {
            ret.add(current_line.toString());
        }
        return ret.toArray(EMPTY_STR_ARR);
    }

}
