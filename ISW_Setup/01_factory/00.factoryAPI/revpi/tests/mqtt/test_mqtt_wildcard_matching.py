import pytest

from revpi.mqtt import get_wildcard_topic

wildcard_matches = [
    ('a/b/c/d', 'a/b/c/d', []),

    ('+/b/c/d', 'a/b/c/d', ['a']),
    ('a/+/+/d', 'a/b/c/d', ['b', 'c']),
    ('a/+/c/d', 'a/b/c/d', ['b']),
    ('+/+/+/+', 'a/b/c/d', ['a', 'b', 'c', 'd']),

    ('#', 'a/b/c/d', [['a', 'b', 'c', 'd']]),
    ('a/#', 'a/b/c/d', [['b', 'c', 'd']]),
    ('a/b/#', 'a/b/c/d', [['c', 'd']]),
    ('a/b/c/#', 'a/b/c/d', [['d']]),

    ('+/b/c/#', 'a/b/c/d', ['a', ['d']]),

    ('a/+/topic', 'a//topic', ['']),
    ('#', '/a/topic', [['', 'a', 'topic']]),
    ('/#', '/a/topic', [['a', 'topic']]),
    ('a/topic/+', 'a/topic/', ['']),
    ('a/topic/#', 'a/topic/', [['']]),
]

wildcard_mismatches = [
    ('a/b/c', 'a/b/c/d'),
    ('b/+/c/d', 'a/b/c/d'),
    ('+/+/+', 'a/b/c/d'),
    ('#/d', 'a/b/c/d')
]


@pytest.mark.parametrize('subscription, topic, result', wildcard_matches)
def test_wildcard_matches(subscription, topic, result):
    wildcards = get_wildcard_topic(subscription, topic)

    assert wildcards == result
