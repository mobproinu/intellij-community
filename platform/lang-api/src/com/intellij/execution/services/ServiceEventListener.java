// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.execution.services;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface ServiceEventListener {
  Topic<ServiceEventListener> TOPIC =
    Topic.create("services topic", ServiceEventListener.class, Topic.BroadcastDirection.TO_CHILDREN);

  void handle(@NotNull ServiceEvent event);

  final class ServiceEvent {
    public final EventType type;
    public final Object target;
    public final Class<?> contributorClass;

    public final Object parent;
    public final int index;

    private ServiceEvent(@NotNull EventType type,
                         @NotNull Object target,
                         @NotNull Class<?> contributorClass) {
      this(type, target, contributorClass, null, -1);
    }

    private ServiceEvent(@NotNull EventType type,
                         @NotNull Object target,
                         @NotNull Class<?> contributorClass,
                         @Nullable Object parent,
                         int index) {
      this.type = type;
      this.target = target;
      this.contributorClass = contributorClass;
      this.parent = parent;
      this.index = index;
    }

    public static ServiceEvent createEvent(@NotNull EventType type,
                                           @NotNull Object target,
                                           @NotNull Class<?> rootContributorClass) {
      return new ServiceEvent(type, target, rootContributorClass);
    }

    public static ServiceEvent createResetEvent(@NotNull Class<?> rootContributorClass) {
      return new ServiceEvent(EventType.RESET, rootContributorClass, rootContributorClass);
    }

    public static ServiceEvent createServiceAddedEvent(@NotNull Object target,
                                                       @NotNull Class<?> contributorClass,
                                                       @Nullable Object parent,
                                                       int index) {
      return new ServiceEvent(EventType.SERVICE_ADDED, target, contributorClass, parent, index);
    }
  }

  enum EventType {
    RESET,
    SERVICE_ADDED, SERVICE_REMOVED, SERVICE_CHANGED, SERVICE_STRUCTURE_CHANGED,
    GROUP_CHANGED
  }
}
