import z, { Infer } from "myzod";

export const TMessage = z.object({
  message_id: z.number(),
  sender: z.string(),
  minecraft: z.string(),
});

export type IMessage = Infer<typeof TMessage>;
