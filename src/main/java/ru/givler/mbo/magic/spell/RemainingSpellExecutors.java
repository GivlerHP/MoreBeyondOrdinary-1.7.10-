package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.magic.EntityMagicConstruct;
import ru.givler.mbo.entity.magic.EntityMagicDecoy;
import ru.givler.mbo.magic.api.*;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock;

/** Native MBO implementations for the remaining non-summoning spells. */
public final class RemainingSpellExecutors {
  private static final String CLAIR_X = "mboClairvoyanceX",
      CLAIR_Y = "mboClairvoyanceY",
      CLAIR_Z = "mboClairvoyanceZ",
      CLAIR_DIM = "mboClairvoyanceDimension",
      CLAIR_SET = "mboClairvoyanceSet";

  private RemainingSpellExecutors() {}

  public static SpellExecutor clairvoyance() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        if (!(c.caster() instanceof EntityPlayer)) return SpellResult.BLOCKED;
        EntityPlayer p = (EntityPlayer) c.caster();
        if (p.isSneaking()) {
          MovingObjectPosition hit = blockTrace(c, 64);
          if (hit == null) return SpellResult.PASS;
          NBTTagCompound data = clairvoyanceData(c);
          data.setInteger(CLAIR_X, hit.blockX);
          data.setInteger(CLAIR_Y, hit.blockY);
          data.setInteger(CLAIR_Z, hit.blockZ);
          data.setInteger(CLAIR_DIM, p.dimension);
          data.setBoolean(CLAIR_SET, true);
          if (!c.world().isRemote)
            p.addChatMessage(
                new ChatComponentText(
                    "Точка ясновидения сохранена: "
                        + hit.blockX
                        + ", "
                        + hit.blockY
                        + ", "
                        + hit.blockZ));
          return SpellResult.BLOCKED;
        }
        NBTTagCompound data = clairvoyanceData(c);
        if (!data.getBoolean(CLAIR_SET)) {
          if (!c.world().isRemote)
            p.addChatMessage(new ChatComponentTranslation("mbo.spell.clairvoyance.undefined"));
          return SpellResult.PASS;
        }
        if (data.getInteger(CLAIR_DIM) != p.dimension) {
          if (!c.world().isRemote)
            p.addChatMessage(
                new ChatComponentTranslation("mbo.spell.clairvoyance.wrong_dimension"));
          return SpellResult.PASS;
        }
        PathEntity path =
            c.world()
                .getEntityPathToXYZ(
                    p,
                    data.getInteger(CLAIR_X),
                    data.getInteger(CLAIR_Y),
                    data.getInteger(CLAIR_Z),
                    256 * c.range(),
                    true,
                    true,
                    false,
                    true);
        if (path == null) {
          if (!c.world().isRemote)
            p.addChatMessage(new ChatComponentText("Путь к сохранённой точке не найден."));
          return SpellResult.PASS;
        }
        if (c.world().isRemote)
          for (int i = 0; i < path.getCurrentPathLength() - 1; i += 2) {
            PathPoint q = path.getPathPointFromIndex(i),
                n = path.getPathPointFromIndex(Math.min(i + 2, path.getCurrentPathLength() - 1));
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.PATH,
                c.world(),
                q.xCoord + .5,
                q.yCoord + .5,
                q.zCoord + .5,
                (n.xCoord - q.xCoord) / 45D,
                (n.yCoord - q.yCoord) / 45D,
                (n.zCoord - q.zCoord) / 45D,
                new ParticleSettings((int) (1800 * c.duration()), 0, 1, .3F, 1.25F, false));
          }
        c.world().playSoundAtEntity(p, "mbo:aura", 1, 1);
        return SpellResult.SUCCESS;
      }
    };
  }

  private static NBTTagCompound clairvoyanceData(SpellContext c) {
    if (c.sourceItem() != null) {
      if (!c.sourceItem().hasTagCompound()) c.sourceItem().setTagCompound(new NBTTagCompound());
      return c.sourceItem().getTagCompound();
    }
    return c.caster().getEntityData();
  }

  public static SpellExecutor soulbinding() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        EntityLivingBase t = target(c, 10, .25);
        if (t == null) return SpellResult.PASS;
        if (!c.world().isRemote) {
          t.getEntityData().setString("mboSoulboundCaster", c.caster().getUniqueID().toString());
          c.caster().getEntityData().setString("mboSoulboundVictim", t.getUniqueID().toString());
          SpellEffects.sparkleBurst(t, 24, .35F, 0, .45F);
        }
        c.world().playSoundAtEntity(c.caster(), "mob.wither.spawn", 1, 1);
        return SpellResult.SUCCESS;
      }
    };
  }

  public static SpellExecutor decoy() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        c.world()
            .playSoundAtEntity(
                c.caster(), "mbo:aura", 1.0F, 0.4F / (c.world().rand.nextFloat() * 0.4F + 0.8F));
        if (c.world().isRemote) return SpellResult.SUCCESS;
        EntityMagicDecoy d =
            new EntityMagicDecoy(c.world(), c.caster(), Math.max(1, (int) (600 * c.duration())));
        c.world().spawnEntityInWorld(d);
        @SuppressWarnings("unchecked")
        List<EntityLiving> mobs =
            c.world()
                .getEntitiesWithinAABB(
                    EntityLiving.class, c.caster().boundingBox.expand(16, 8, 16));
        for (EntityLiving mob : mobs)
          if (mob != d && mob.getAttackTarget() == c.caster() && c.world().rand.nextBoolean())
            mob.setAttackTarget(d);
        return SpellResult.SUCCESS;
      }
    };
  }

  public static SpellExecutor construct(final EntityMagicConstruct.Kind kind) {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        if (kind == EntityMagicConstruct.Kind.EARTHQUAKE && !c.caster().onGround)
          return SpellResult.PASS;
        double x = c.caster().posX, y = c.caster().boundingBox.minY, z = c.caster().posZ;
        if (kind == EntityMagicConstruct.Kind.HAMMER) {
          MovingObjectPosition h = blockTrace(c, 40);
          if (h == null || !c.world().canBlockSeeTheSky(h.blockX, h.blockY + 1, h.blockZ))
            return SpellResult.PASS;
          x = h.blockX + .5;
          y = h.blockY + 50;
          z = h.blockZ + .5;
        }
        if (kind == EntityMagicConstruct.Kind.TORNADO) {
          Vec3 l = c.caster().getLookVec();
          x += l.xCoord;
          y += .1;
          z += l.zCoord;
        }
        if (!c.world().isRemote)
          c.world()
              .spawnEntityInWorld(
                  new EntityMagicConstruct(
                      c.world(), x, y, z, c.caster(), kind, c.power(), c.duration(), c.area()));
        if (kind == EntityMagicConstruct.Kind.EARTHQUAKE) {
          if (!c.world().isRemote) c.world().playSoundAtEntity(c.caster(), "mbo:rumble", 2, 1);
          else {
            c.world().spawnParticle("largeexplode", x, y + .1, z, 0, 0, 0);
            int bx = MathHelper.floor_double(x),
                by = MathHelper.floor_double(y) - 1,
                bz = MathHelper.floor_double(z);
            Block block = c.world().getBlock(bx, by, bz);
            for (int i = 0; i < 40; i++) {
              double px = x - 1 + 2 * c.world().rand.nextDouble(),
                  pz = z - 1 + 2 * c.world().rand.nextDouble();
              c.world()
                  .spawnParticle(
                      "blockcrack_"
                          + Block.getIdFromBlock(block)
                          + "_"
                          + c.world().getBlockMetadata(bx, by, bz),
                      px,
                      y,
                      pz,
                      px - x,
                      0,
                      pz - z);
            }
          }
        } else if (kind == EntityMagicConstruct.Kind.HAMMER)
          c.world().playSoundAtEntity(c.caster(), "mbo:darkaura", 3, 1);
        else c.world().playSoundAtEntity(c.caster(), "mbo:ice", 1, 1);
        c.caster().swingItem();
        return SpellResult.SUCCESS;
      }
    };
  }

  public static SpellExecutor forestsCurse() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        double radius = 5.0D * c.area();
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> list =
            c.world()
                .getEntitiesWithinAABB(
                    EntityLivingBase.class, c.caster().boundingBox.expand(radius, radius, radius));
        if (!c.world().isRemote) {
          for (EntityLivingBase t : list) {
            if (t != c.caster() && t.isEntityAlive() && !t.isOnSameTeam(c.caster())) {
              t.attackEntityFrom(
                  new EntityDamageSource("mbo.magic", c.caster()).setMagicDamage(),
                  4.0F * c.power());
              int dur = (int) (140 * c.duration());
              t.addPotionEffect(new PotionEffect(Potion.poison.id, dur, 2));
              t.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, dur, 2));
              t.addPotionEffect(new PotionEffect(Potion.weakness.id, dur, 2));
            }
          }
        }
        if (c.world().isRemote) {
          double eyeY = c.caster().posY + c.caster().getEyeHeight() + 0.5D;
          int particleCount = (int) (50 * c.area());
          for (int i = 0; i < particleCount; i++) {
            double r = (1.0D + c.world().rand.nextDouble() * 4.0D) * c.area();
            double angle = c.world().rand.nextDouble() * Math.PI * 2.0D;
            double px = c.caster().posX + r * Math.cos(angle);
            double pz = c.caster().posZ + r * Math.sin(angle);
            float brightness = c.world().rand.nextFloat() / 4.0F;
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.DARK_MAGIC,
                c.world(),
                px,
                eyeY,
                pz,
                0,
                -0.2D,
                0,
                new ParticleSettings(
                    12, 0.05F + brightness, 0.2F + brightness, 0.0F, 0.75F, false));
            brightness = c.world().rand.nextFloat() / 4.0F;
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.SPARKLE,
                c.world(),
                px,
                eyeY,
                pz,
                0,
                -0.05D,
                0,
                new ParticleSettings(50, 0.1F + brightness, 0.2F + brightness, 0.0F, 0.75F, false));
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.LEAF,
                c.world(),
                px,
                eyeY,
                pz,
                0,
                -0.01D,
                0,
                new ParticleSettings(
                    40 + c.world().rand.nextInt(12), 1.0F, 1.0F, 1.0F, 1.4F, false));
          }
        }
        c.caster().swingItem();
        c.world()
            .playSoundAtEntity(
                c.caster(), "mob.wither.spawn", 1.0F, c.world().rand.nextFloat() * 0.2F + 1.0F);
        return SpellResult.SUCCESS;
      }
    };
  }

  public static SpellExecutor plague() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        double radius = 5 * c.area();
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> list =
            c.world()
                .getEntitiesWithinAABB(
                    EntityLivingBase.class, c.caster().boundingBox.expand(radius, radius, radius));
        if (!c.world().isRemote)
          for (EntityLivingBase t : list)
            if (t != c.caster() && !t.isOnSameTeam(c.caster())) {
              t.attackEntityFrom(
                  new EntityDamageSource("mbo.dark_plague", c.caster()).setMagicDamage(),
                  8 * c.power());
              t.addPotionEffect(new PotionEffect(Potion.wither.id, (int) (140 * c.duration()), 2));
            } else {
            }
        if (c.world().isRemote)
          for (int i = 0; i < (int) (40 * c.area()); i++) {
            double x = c.caster().posX - 1 + 2 * c.world().rand.nextDouble(),
                z = c.caster().posZ - 1 + 2 * c.world().rand.nextDouble(),
                y = c.caster().boundingBox.minY;
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.DARK_MAGIC,
                c.world(),
                x,
                y,
                z,
                x - c.caster().posX,
                0,
                z - c.caster().posZ,
                new ParticleSettings(20, .1F, 0, .1F, .9F, false));
            MoreBeyondOrdinary.proxy.spawnParticle(
                EnumParticleType.SPARKLE,
                c.world(),
                x,
                y,
                z,
                x - c.caster().posX,
                0,
                z - c.caster().posZ,
                new ParticleSettings(30, .1F, 0, .05F, .8F, false));
            Block block =
                c.world()
                    .getBlock(
                        MathHelper.floor_double(x),
                        MathHelper.floor_double(y) - 1,
                        MathHelper.floor_double(z));
            c.world()
                .spawnParticle(
                    "blockcrack_" + Block.getIdFromBlock(block) + "_0",
                    x,
                    y,
                    z,
                    x - c.caster().posX,
                    0,
                    z - c.caster().posZ);
          }
        c.caster().swingItem();
        c.world()
            .playSoundAtEntity(
                c.caster(), "mob.wither.death", 1, c.world().rand.nextFloat() * .2F + 1);
        return SpellResult.SUCCESS;
      }
    };
  }

  private static SpellExecutor areaCurse(
      final Potion a,
      final Potion b,
      final Potion d,
      final float damage,
      final double radius,
      final int time) {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> list =
            c.world()
                .getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    c.caster()
                        .boundingBox
                        .expand(radius * c.area(), radius * c.area(), radius * c.area()));
        if (!c.world().isRemote)
          for (EntityLivingBase t : list)
            if (t != c.caster() && !t.isOnSameTeam(c.caster())) {
              t.attackEntityFrom(
                  new EntityDamageSource("mbo.magic", c.caster()).setMagicDamage(),
                  damage * c.power());
              t.addPotionEffect(new PotionEffect(a.id, (int) (time * c.duration()), 2));
              if (b != null)
                t.addPotionEffect(new PotionEffect(b.id, (int) (time * c.duration()), 2));
              if (d != null)
                t.addPotionEffect(new PotionEffect(d.id, (int) (time * c.duration()), 2));
            }
        c.world().playSoundAtEntity(c.caster(), "mob.wither.death", 1, 1);
        return SpellResult.SUCCESS;
      }
    };
  }

  public static SpellExecutor metamorphosis() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        EntityLivingBase old = target(c, 10, 1.0);
        if (!(old instanceof EntityLiving)) return SpellResult.PASS;
        EntityLiving replacement = replacement(c, old);
        if (old instanceof EntitySkeleton) {
          EntitySkeleton s = (EntitySkeleton) old;
          if (!c.world().isRemote) s.setSkeletonType(s.getSkeletonType() == 0 ? 1 : 0);
          metamorphosisVisuals(c, old);
          c.world().playSoundAtEntity(c.caster(), "mbo:effect", .5F, .8F);
          c.caster().swingItem();
          return SpellResult.SUCCESS;
        }
        if (replacement == null) return SpellResult.PASS;
        metamorphosisVisuals(c, old);
        if (!c.world().isRemote) {
          replacement.setLocationAndAngles(
              old.posX, old.posY, old.posZ, old.rotationYaw, old.rotationPitch);
          replacement.setHealth(Math.min(old.getHealth(), replacement.getMaxHealth()));
          old.setDead();
          c.world().spawnEntityInWorld(replacement);
        }
        c.world().playSoundAtEntity(c.caster(), "mbo:effect", .5F, .8F);
        c.caster().swingItem();
        return SpellResult.SUCCESS;
      }
    };
  }

  private static void metamorphosisVisuals(SpellContext c, EntityLivingBase target) {
    if (!c.world().isRemote) return;
    Vec3 look = c.caster().getLookVec();
    for (int i = 1; i < (int) (25 * c.range()); i += 2) {
      double x = c.caster().posX + look.xCoord * i / 2 + c.world().rand.nextFloat() / 5 - .1,
          y =
              c.caster().posY
                  + c.caster().getEyeHeight()
                  - .4
                  + look.yCoord * i / 2
                  + c.world().rand.nextFloat() / 5
                  - .1,
          z = c.caster().posZ + look.zCoord * i / 2 + c.world().rand.nextFloat() / 5 - .1;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          c.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(12 + c.world().rand.nextInt(8), .2F, 0, .1F, .75F, false));
    }
    for (int i = 0; i < 5; i++)
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DARK_MAGIC,
          c.world(),
          target.posX,
          target.posY,
          target.posZ,
          0,
          0,
          0,
          new ParticleSettings(24, .1F, 0, 0, .8F, false));
  }

  private static EntityLiving replacement(SpellContext c, EntityLivingBase e) {
    if (e instanceof EntityPig) return new EntityPigZombie(c.world());
    if (e instanceof EntityPigZombie) return new EntityPig(c.world());
    if (e instanceof EntityMooshroom) return new EntityCow(c.world());
    if (e instanceof EntityCow) return new EntityMooshroom(c.world());
    if (e instanceof EntityChicken) return new EntityBat(c.world());
    if (e instanceof EntityBat) return new EntityChicken(c.world());
    if (e instanceof EntityMagmaCube) return new EntitySlime(c.world());
    if (e instanceof EntitySlime) return new EntityMagmaCube(c.world());
    if (e instanceof EntityCaveSpider) return new EntitySpider(c.world());
    if (e instanceof EntitySpider) return new EntityCaveSpider(c.world());
    return null;
  }

  public static SpellExecutor spectralPathway() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        if (!c.caster().onGround) return SpellResult.PASS;
        Vec3 look = c.caster().getLookVec();
        double length = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
        if (length < .01D) return SpellResult.PASS;
        double forwardX = look.xCoord / length, forwardZ = look.zCoord / length;
        int floorY = MathHelper.floor_double(c.caster().boundingBox.minY) - 1;
        Block standing =
            c.world()
                .getBlock(
                    MathHelper.floor_double(c.caster().posX),
                    floorY,
                    MathHelper.floor_double(c.caster().posZ));
        if (standing == Blocks.air || standing == BlockRegistry.TemporarySpectral)
          return SpellResult.PASS;
        boolean placed = false;
        if (!c.world().isRemote)
          for (int distance = 1; distance <= (int) (15 * c.range()); distance++)
            for (int side = -1; side <= 1; side++) {
              int x =
                  MathHelper.floor_double(c.caster().posX + forwardX * distance - forwardZ * side);
              int z =
                  MathHelper.floor_double(c.caster().posZ + forwardZ * distance + forwardX * side);
              Block old = c.world().getBlock(x, floorY, z);
              if ((c.world().isAirBlock(x, floorY, z) || old.isReplaceable(c.world(), x, floorY, z))
                  && !old.getMaterial().isLiquid()) {
                c.world().setBlock(x, floorY, z, BlockRegistry.TemporarySpectral);
                if (c.world().getTileEntity(x, floorY, z) instanceof TileEntityTemporaryMagicBlock)
                  ((TileEntityTemporaryMagicBlock) c.world().getTileEntity(x, floorY, z))
                      .setLifetime((int) (1200 * c.duration()));
                placed = true;
              }
            }
        if (placed) {
          c.world().playSoundAtEntity(c.caster(), "mbo:largeaura", 1, 1);
          return SpellResult.SUCCESS;
        }
        return c.world().isRemote ? SpellResult.SUCCESS : SpellResult.PASS;
      }
    };
  }

  public static SpellExecutor telekinesis() {
    return new SpellExecutor() {
      public SpellResult cast(SpellContext c) {
        Entity entity = entityInSight(c, 8, 3.0);
        if (entity instanceof EntityItem) {
          entity.motionX = (c.caster().posX - entity.posX) / 6;
          entity.motionY = (c.caster().posY + c.caster().getEyeHeight() - entity.posY) / 6;
          entity.motionZ = (c.caster().posZ - entity.posZ) / 6;
          c.world().playSoundAtEntity(entity, "mbo:aura", 1, 1);
          c.caster().swingItem();
          return SpellResult.SUCCESS;
        }
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getHeldItem() != null) {
          if (!c.world().isRemote) {
            EntityPlayer p = (EntityPlayer) entity;
            EntityItem item = p.entityDropItem(p.getHeldItem(), 0);
            item.motionX = (c.caster().posX - p.posX) / 20;
            item.motionZ = (c.caster().posZ - p.posZ) / 20;
            p.setCurrentItemOrArmor(0, null);
          }
          c.world().playSoundAtEntity(entity, "mbo:aura", 1, 1);
          c.caster().swingItem();
          return SpellResult.SUCCESS;
        }
        MovingObjectPosition hit = blockTrace(c, 8);
        if (hit != null && c.caster() instanceof EntityPlayer) {
          Block block = c.world().getBlock(hit.blockX, hit.blockY, hit.blockZ);
          float hx = (float) (hit.hitVec.xCoord - hit.blockX),
              hy = (float) (hit.hitVec.yCoord - hit.blockY),
              hz = (float) (hit.hitVec.zCoord - hit.blockZ);
          if (block.onBlockActivated(
              c.world(),
              hit.blockX,
              hit.blockY,
              hit.blockZ,
              (EntityPlayer) c.caster(),
              hit.sideHit,
              hx,
              hy,
              hz)) {
            c.world().playSound(hit.blockX, hit.blockY, hit.blockZ, "mbo:aura", 1, 1, false);
            c.caster().swingItem();
            return SpellResult.SUCCESS;
          }
        }
        return SpellResult.PASS;
      }
    };
  }

  private static EntityLivingBase target(SpellContext c, double range, double margin) {
    return c.target() != null
        ? c.target()
        : SpellTargeting.livingNearSight(c.caster(), range * c.range(), margin);
  }

  private static MovingObjectPosition blockTrace(SpellContext c, double range) {
    Vec3
        s =
            Vec3.createVectorHelper(
                c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ),
        l = c.caster().getLookVec();
    return c.world()
        .rayTraceBlocks(
            s,
            s.addVector(
                l.xCoord * range * c.range(),
                l.yCoord * range * c.range(),
                l.zCoord * range * c.range()));
  }

  @SuppressWarnings("unchecked")
  private static Entity entityInSight(SpellContext c, double range, double margin) {
    Vec3
        s =
            Vec3.createVectorHelper(
                c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ),
        l = c.caster().getLookVec(),
        e = s.addVector(l.xCoord * range, l.yCoord * range, l.zCoord * range);
    Entity best = null;
    double dist = range;
    for (Entity x :
        (List<Entity>)
            c.world()
                .getEntitiesWithinAABBExcludingEntity(
                    c.caster(),
                    c.caster()
                        .boundingBox
                        .addCoord(l.xCoord * range, l.yCoord * range, l.zCoord * range)
                        .expand(margin, margin, margin))) {
      MovingObjectPosition h =
          x.boundingBox.expand(margin, margin, margin).calculateIntercept(s, e);
      if (h != null && s.distanceTo(h.hitVec) < dist) {
        best = x;
        dist = s.distanceTo(h.hitVec);
      }
    }
    return best;
  }
}
