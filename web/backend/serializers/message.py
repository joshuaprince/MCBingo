from rest_framework import serializers


class MessageSerializer(serializers.Serializer):
    message_id = serializers.SerializerMethodField()
    sender = serializers.CharField(max_length=1024)
    minecraft = serializers.JSONField()

    _last_id = 0  # TODO

    def get_message_id(self, obj):
        self._last_id += 1
        return self._last_id

    def update(self, instance, validated_data):
        raise NotImplementedError("Cannot update a MessageSerializer.")

    def create(self, validated_data):
        raise NotImplementedError("Cannot create a MessageSerializer.")
