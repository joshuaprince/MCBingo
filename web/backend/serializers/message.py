from rest_framework import serializers


class MessageSerializer(serializers.Serializer):
    sender = serializers.CharField(max_length=1024)
    mc_tellraw = serializers.JSONField(required=False)
    mc_minimessage = serializers.CharField(required=False)

    def update(self, instance, validated_data):
        raise NotImplementedError("Cannot update a MessageSerializer.")

    def create(self, validated_data):
        raise NotImplementedError("Cannot create a MessageSerializer.")
