//
//  Copyright (c) 2014 VK.com
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of
//  this software and associated documentation files (the "Software"), to deal in
//  the Software without restriction, including without limitation the rights to
//  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
//  the Software, and to permit persons to whom the Software is furnished to do so,
//  subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
//  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
//  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
//  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//


package com.vk.sdk.api;

import android.support.annotation.NonNull;

class VKSyncRequestUtil {

    private static class Listener extends VKRequest.VKRequestListener {

        private final Object syncObj = new Object();
        private VKRequest.VKRequestListener listener;
        private volatile boolean isFinish = false;

        public Listener(VKRequest.VKRequestListener listener) {
            this.listener = listener;
        }

        @Override
        public void onComplete(VKResponse response) {
            synchronized (this.syncObj) {
                try {
                    listener.onComplete(response);
                } catch (Exception e) {
                    // nothing
                }
                isFinish = true;
                syncObj.notifyAll();
            }
        }

        @Override
        public void onError(VKError error) {
            synchronized (this.syncObj) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    // nothing
                }
                isFinish = true;
                syncObj.notifyAll();
            }
        }
    }

    public static void executeSyncWithListener(@NonNull VKRequest vkRequest, @NonNull VKRequest.VKRequestListener vkListener) {
        Listener listener = new Listener(vkListener);
        vkRequest.setUseLooperForCallListener(false);
        vkRequest.executeWithListener(listener);
        synchronized (listener.syncObj) {
            while (!listener.isFinish) {
                try {
                    listener.syncObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}