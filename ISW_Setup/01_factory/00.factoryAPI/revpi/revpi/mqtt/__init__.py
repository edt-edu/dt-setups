from typing import List, Union


def machine_control_topic(machine: str):
    """MQTT topic for machine controls"""
    return f"{machine}/control"


def machine_status_topic(machine: str):
    """MQTT topic for machine status"""
    return f"{machine}/status"


def machine_lifecycle_topic(island: str, machine: str):
    """ MQTT topic for machine lifecycle
    """
    return f"{island}/machines/{machine}"


def get_wildcard_topic(subscription: str, topic: str):
    """ Get a list of all the wildcards matches

    No other matching test is performed.
    """
    subs = subscription.split('/')
    topics = topic.split('/')
    wildcards: List[Union[str, List[str]]] = []
    for i in range(len(subs)):
        if subs[i] == '+':
            wildcards.append(topics[i])
        elif subs[i] == '#':
            wildcards.append(topics[i:])
            break
    return wildcards
